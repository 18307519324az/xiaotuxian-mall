<template>
  <Teleport to="body">
    <div class="cropper-overlay" @mousedown="onOverlayClick" @contextmenu.prevent>
      <div class="cropper-modal" @mousedown.stop>
        <!-- Header -->
        <div class="cropper-header">
          <h3>更换头像</h3>
        </div>

        <!-- Body -->
        <div class="cropper-body">
          <!-- Left: Crop area -->
          <div class="crop-area" ref="cropAreaRef"
            @mousedown="onDragStart"
            @mousemove="onDragMove"
            @mouseup="onDragEnd"
            @mouseleave="onDragEnd"
            @wheel.prevent="onWheel">
            <div class="crop-stage" :style="stageStyle">
              <canvas ref="mainCanvas" :width="cropCanvasSize" :height="cropCanvasSize"></canvas>
            </div>
            <div class="circle-overlay"></div>
            <div class="crop-hint">拖动图片调整位置</div>
          </div>

          <!-- Right: Preview -->
          <div class="preview-area">
            <div class="preview-circle">
              <canvas ref="previewCanvas" width="150" height="150"></canvas>
            </div>
            <p class="preview-label">头像将这样显示</p>

            <!-- Zoom control -->
            <div class="zoom-control">
              <span class="zoom-label">缩放</span>
              <input type="range" class="zoom-slider" min="50" max="300" value="100"
                @input="onZoomChange" ref="zoomSlider" />
              <span class="zoom-value">{{ Math.round(scale * 100) }}%</span>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="cropper-footer">
          <button class="btn-cancel" @click="onCancel">取消</button>
          <button class="btn-confirm" @click="onConfirm">确认裁剪</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script>
import { ref, watch, onMounted, nextTick } from "vue";

const CROP_SIZE = 300;       // output canvas size
const DISPLAY_SIZE = 380;    // crop area display size (CSS)
const CIRCLE_RADIUS = 140;   // visible circle radius in the crop display
const PREVIEW_SIZE = 150;    // preview canvas size

export default {
  name: "AvatarCropper",
  props: {
    imageSource: { type: String, required: true },
  },
  emits: ["confirm", "cancel"],
  setup(props, { emit }) {
    const cropAreaRef = ref(null);
    const mainCanvas = ref(null);
    const previewCanvas = ref(null);
    const zoomSlider = ref(null);

    const cropCanvasSize = ref(DISPLAY_SIZE);

    // Image state
    const img = ref(null);
    const scale = ref(1.0);
    const offsetX = ref(0);
    const offsetY = ref(0);
    const isDragging = ref(false);
    const dragStartX = ref(0);
    const dragStartY = ref(0);
    const dragOriginX = ref(0);
    const dragOriginY = ref(0);

    // Image original dimensions
    const imgNaturalW = ref(0);
    const imgNaturalH = ref(0);

    // Fit image to crop area initially
    function calcFitScale(iw, ih) {
      const viewSize = DISPLAY_SIZE;
      const fit = Math.min(viewSize / iw, viewSize / ih) * 0.85;
      return Math.max(fit, 0.1);
    }

    function loadImage(src) {
      return new Promise((resolve, reject) => {
        const image = new Image();
        image.onload = () => resolve(image);
        image.onerror = () => reject(new Error("图片加载失败"));
        image.src = src;
      });
    }

    // Draw the main crop canvas
    function drawMain() {
      const canvas = mainCanvas.value;
      if (!canvas || !img.value) return;
      const ctx = canvas.getContext("2d");
      const w = canvas.width;
      const h = canvas.height;

      ctx.clearRect(0, 0, w, h);

      // Draw image centered with offset, clipped to circle
      ctx.save();
      ctx.beginPath();
      ctx.arc(w / 2, h / 2, CIRCLE_RADIUS, 0, Math.PI * 2);
      ctx.clip();

      const drawW = imgNaturalW.value * scale.value;
      const drawH = imgNaturalH.value * scale.value;
      const cx = w / 2 + offsetX.value - drawW / 2;
      const cy = h / 2 + offsetY.value - drawH / 2;
      ctx.drawImage(img.value, cx, cy, drawW, drawH);

      ctx.restore();

      // Draw a subtle border on the circle
      ctx.beginPath();
      ctx.arc(w / 2, h / 2, CIRCLE_RADIUS, 0, Math.PI * 2);
      ctx.strokeStyle = "rgba(255,255,255,0.5)";
      ctx.lineWidth = 2;
      ctx.stroke();

      // Draw preview
      drawPreview();
    }

    // Draw the preview canvas (real-time small circle)
    function drawPreview() {
      const canvas = previewCanvas.value;
      if (!canvas || !img.value) return;
      const ctx = canvas.getContext("2d");
      const pw = canvas.width;
      const ph = canvas.height;

      ctx.clearRect(0, 0, pw, ph);

      // Circular clip
      ctx.save();
      ctx.beginPath();
      ctx.arc(pw / 2, ph / 2, pw / 2, 0, Math.PI * 2);
      ctx.clip();

      // Map the crop circle area to preview
      const viewSize = DISPLAY_SIZE;
      const drawW = imgNaturalW.value * scale.value;
      const drawH = imgNaturalH.value * scale.value;
      const cx = viewSize / 2 + offsetX.value - drawW / 2;
      const cy = viewSize / 2 + offsetY.value - drawH / 2;

      // Scale from display size to preview size
      const ratio = pw / viewSize;
      ctx.drawImage(img.value, cx * ratio, cy * ratio, drawW * ratio, drawH * ratio);

      ctx.restore();
    }

    // Get the final cropped dataURL (300x300)
    function getCroppedDataURL() {
      const canvas = document.createElement("canvas");
      canvas.width = CROP_SIZE;
      canvas.height = CROP_SIZE;
      const ctx = canvas.getContext("2d");

      // Circular clip
      ctx.save();
      ctx.beginPath();
      ctx.arc(CROP_SIZE / 2, CROP_SIZE / 2, CROP_SIZE / 2, 0, Math.PI * 2);
      ctx.clip();

      // Map from display coordinates to crop coordinates
      const viewSize = DISPLAY_SIZE;
      const ratio = CROP_SIZE / viewSize;
      const drawW = imgNaturalW.value * scale.value * ratio;
      const drawH = imgNaturalH.value * scale.value * ratio;
      const cx = (viewSize / 2 + offsetX.value - drawW / ratio / 2) * ratio;
      const cy = (viewSize / 2 + offsetY.value - drawH / ratio / 2) * ratio;

      ctx.drawImage(img.value, cx, cy, drawW, drawH);
      ctx.restore();

      return canvas.toDataURL("image/png");
    }

    // --- Event handlers ---

    function onDragStart(e) {
      if (e.button !== 0) return;
      isDragging.value = true;
      dragStartX.value = e.clientX;
      dragStartY.value = e.clientY;
      dragOriginX.value = offsetX.value;
      dragOriginY.value = offsetY.value;
      cropAreaRef.value?.classList.add("dragging");
    }

    function onDragMove(e) {
      if (!isDragging.value) return;
      const dx = e.clientX - dragStartX.value;
      const dy = e.clientY - dragStartY.value;
      offsetX.value = dragOriginX.value + dx;
      offsetY.value = dragOriginY.value + dy;
      drawMain();
    }

    function onDragEnd() {
      if (!isDragging.value) return;
      isDragging.value = false;
      cropAreaRef.value?.classList.remove("dragging");
    }

    function onWheel(e) {
      const delta = e.deltaY > 0 ? -0.05 : 0.05;
      scale.value = Math.max(0.5, Math.min(3.0, scale.value + delta));
      if (zoomSlider.value) {
        zoomSlider.value.value = Math.round(scale.value * 100);
      }
      drawMain();
    }

    function onZoomChange(e) {
      scale.value = parseInt(e.target.value) / 100;
      drawMain();
    }

    function onOverlayClick(e) {
      if (e.target.classList.contains("cropper-overlay")) {
        emit("cancel");
      }
    }

    function onCancel() {
      emit("cancel");
    }

    function onConfirm() {
      const dataURL = getCroppedDataURL();
      emit("confirm", dataURL);
    }

    // Watch for image source changes
    watch(() => props.imageSource, async (src) => {
      if (!src) return;
      try {
        const image = await loadImage(src);
        img.value = image;
        imgNaturalW.value = image.naturalWidth;
        imgNaturalH.value = image.naturalHeight;
        scale.value = calcFitScale(imgNaturalW.value, imgNaturalH.value);
        offsetX.value = 0;
        offsetY.value = 0;
        if (zoomSlider.value) {
          zoomSlider.value.value = Math.round(scale.value * 100);
        }
        await nextTick();
        drawMain();
      } catch (e) {
        console.error("Failed to load image for cropping:", e);
        emit("cancel");
      }
    }, { immediate: true });

    onMounted(async () => {
      await nextTick();
      if (props.imageSource) {
        try {
          const image = await loadImage(props.imageSource);
          img.value = image;
          imgNaturalW.value = image.naturalWidth;
          imgNaturalH.value = image.naturalHeight;
          scale.value = calcFitScale(imgNaturalW.value, imgNaturalH.value);
          if (zoomSlider.value) {
            zoomSlider.value.value = Math.round(scale.value * 100);
          }
          await nextTick();
          drawMain();
        } catch (e) {
          console.error("Failed to load image for cropping:", e);
          emit("cancel");
        }
      }
    });

    return {
      cropAreaRef, mainCanvas, previewCanvas, zoomSlider,
      cropCanvasSize, scale, offsetX, offsetY,
      onDragStart, onDragMove, onDragEnd, onWheel, onZoomChange,
      onOverlayClick, onCancel, onConfirm,
    };
  },
};
</script>

<style scoped lang="less">
.cropper-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  user-select: none;
}

.cropper-modal {
  background: #fff;
  border-radius: 12px;
  width: 780px;
  max-width: 95vw;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  overflow: hidden;
}

.cropper-header {
  padding: 20px 28px 0;
  h3 {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin: 0;
  }
}

.cropper-body {
  display: flex;
  gap: 24px;
  padding: 20px 28px;
}

.crop-area {
  position: relative;
  width: 380px;
  height: 380px;
  border-radius: 50%;
  overflow: hidden;
  background: #f0f0f0;
  cursor: grab;
  flex-shrink: 0;

  &.dragging {
    cursor: grabbing;
  }

  .crop-stage {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;

    canvas {
      display: block;
    }
  }

  .circle-overlay {
    position: absolute;
    inset: 0;
    border-radius: 50%;
    pointer-events: none;
    box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.35);
    border: 2px solid rgba(255, 255, 255, 0.7);
  }

  .crop-hint {
    position: absolute;
    bottom: 12px;
    left: 50%;
    transform: translateX(-50%);
    color: rgba(255, 255, 255, 0.8);
    font-size: 12px;
    background: rgba(0, 0, 0, 0.4);
    padding: 3px 12px;
    border-radius: 10px;
    white-space: nowrap;
    pointer-events: none;
  }
}

.preview-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  min-width: 160px;
}

.preview-circle {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  overflow: hidden;
  background: #f5f5f5;
  border: 3px solid #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);

  canvas {
    display: block;
    width: 100%;
    height: 100%;
  }
}

.preview-label {
  font-size: 13px;
  color: #999;
  margin: 0;
  text-align: center;
}

.zoom-control {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  max-width: 220px;
}

.zoom-label {
  font-size: 13px;
  color: #666;
  white-space: nowrap;
}

.zoom-slider {
  flex: 1;
  height: 4px;
  -webkit-appearance: none;
  appearance: none;
  background: #e0e0e0;
  border-radius: 2px;
  outline: none;

  &::-webkit-slider-thumb {
    -webkit-appearance: none;
    width: 18px;
    height: 18px;
    border-radius: 50%;
    background: @xtxColor;
    cursor: pointer;
    border: 2px solid #fff;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
  }

  &::-moz-range-thumb {
    width: 18px;
    height: 18px;
    border-radius: 50%;
    background: @xtxColor;
    cursor: pointer;
    border: 2px solid #fff;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
  }
}

.zoom-value {
  font-size: 12px;
  color: #999;
  min-width: 40px;
  text-align: right;
}

.cropper-footer {
  padding: 16px 28px 24px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-cancel {
  padding: 8px 24px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: #fff;
  color: #666;
  font-size: 14px;
  cursor: pointer;

  &:hover {
    border-color: #bbb;
    color: #333;
  }
}

.btn-confirm {
  padding: 8px 24px;
  border: none;
  border-radius: 6px;
  background: @xtxColor;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  font-weight: 500;

  &:hover {
    opacity: 0.9;
  }
}
</style>

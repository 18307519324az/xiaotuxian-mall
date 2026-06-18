<template>
  <div class="page-customer-service">
    <div class="cs-container">
      <!-- Left: Quick categories -->
      <aside class="cs-sidebar">
        <h3 class="sidebar-title">快捷问题</h3>
        <ul class="category-list">
          <li v-for="cat in categories" :key="cat.key"
            :class="{ active: activeCategory === cat.key }"
            @click="selectCategory(cat)">
            <span class="cat-icon">{{ cat.icon }}</span>
            <span class="cat-label">{{ cat.label }}</span>
          </li>
        </ul>
        <div class="sidebar-footer">
          <p class="cs-notice">当前为智能客服演示助手，可解答常见购物、订单、售后问题。</p>
        </div>
      </aside>

      <!-- Right: Chat area -->
      <div class="cs-main">
        <!-- Chat header -->
        <div class="chat-header">
          <span class="header-title">在线客服</span>
          <span class="header-status">● 在线</span>
        </div>

        <!-- Messages -->
        <div class="chat-messages" ref="messagesRef">
          <div v-for="(msg, idx) in messages" :key="idx"
            :class="['message', msg.role === 'user' ? 'message-user' : 'message-assistant']">
            <div class="msg-avatar">
              <img v-if="msg.role === 'assistant'" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Ccircle cx='50' cy='50' r='48' fill='%23e4393c'/%3E%3Ctext x='50' y='62' text-anchor='middle' font-size='40' fill='white'%3E✿%3C/text%3E%3C/svg%3E" class="bot-avatar" />
              <div v-else class="user-avatar-icon">
                <i class="iconfont icon-user"></i>
              </div>
            </div>
            <div class="msg-content">
              <div class="msg-text" v-html="formatMessage(msg.content)"></div>
              <div v-if="msg.suggestions && msg.suggestions.length" class="msg-actions">
                <button v-for="(act, aidx) in msg.suggestions" :key="aidx"
                  class="action-btn" @click="handleSuggestion(act)">
                  {{ act }}
                </button>
              </div>
              <div class="msg-time">{{ formatTime(msg.time) }}</div>
            </div>
          </div>

          <!-- Typing indicator -->
          <div v-if="isTyping" class="message message-assistant">
            <div class="msg-avatar">
              <img src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Ccircle cx='50' cy='50' r='48' fill='%23e4393c'/%3E%3Ctext x='50' y='62' text-anchor='middle' font-size='40' fill='white'%3E✿%3C/text%3E%3C/svg%3E" class="bot-avatar" />
            </div>
            <div class="msg-content">
              <div class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- Input area -->
        <div class="chat-input-area">
          <div class="input-wrapper">
            <textarea ref="inputRef" v-model="inputMessage" class="chat-input"
              placeholder="请输入您的问题…" @keydown.enter.prevent="sendMessage"
              :disabled="isTyping" rows="2"></textarea>
          </div>
          <button class="send-btn" @click="sendMessage" :disabled="!inputMessage.trim() || isTyping">
            发送
          </button>
        </div>
      </div>
    </div>

    <!-- Ticket modal -->
    <Teleport to="body">
      <div v-if="ticketModalVisible" class="ticket-overlay" @click.self="closeTicketModal">
        <div class="ticket-modal">
          <h3 class="ticket-title">提交客服工单</h3>
          <div class="ticket-form">
            <div class="field">
              <label>问题类型</label>
              <select v-model="ticketForm.type" class="ticket-select">
                <option value="订单问题">订单问题</option>
                <option value="支付问题">支付问题</option>
                <option value="物流问题">物流问题</option>
                <option value="退换货问题">退换货问题</option>
                <option value="优惠券/礼品卡">优惠券/礼品卡</option>
                <option value="账号问题">账号问题</option>
                <option value="其他">其他</option>
              </select>
            </div>
            <div class="field">
              <label>问题描述</label>
              <textarea v-model="ticketForm.content" class="ticket-textarea"
                placeholder="请详细描述您的问题…" rows="4"></textarea>
            </div>
            <div class="field">
              <label>关联订单（可选）</label>
              <input v-model="ticketForm.orderId" class="ticket-input"
                placeholder="输入订单编号" />
            </div>
          </div>
          <div class="ticket-actions">
            <button class="btn-cancel" @click="closeTicketModal">取消</button>
            <button class="btn-submit" @click="submitTicket"
              :disabled="!ticketForm.type || !ticketForm.content.trim()">提交工单</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script>
import { ref, reactive, onMounted, nextTick, watch } from "vue";
import { useRouter } from "vue-router";
import { customerChat, getCustomerChatHistory, createCustomerTicket, getCustomerTickets } from "@/api/member";
import Message from "@/components/library/Message";

const WELCOME_MESSAGE = {
  role: "assistant",
  content: "您好，我是小兔鲜智能客服助手，可以帮您查询订单、了解支付配送、优惠券、售后等问题。您可以从左侧选择快捷问题，或直接输入您的问题。",
  suggestions: ["我的订单", "怎么退款", "优惠券怎么用", "积分抽奖"],
  time: null,
};

export default {
  name: "MemberCustomerService",
  setup() {
    const router = useRouter();
    const messagesRef = ref(null);
    const inputRef = ref(null);
    const inputMessage = ref("");
    const messages = ref([]);
    const isTyping = ref(false);
    const activeCategory = ref(null);

    // Ticket modal
    const ticketModalVisible = ref(false);
    const ticketForm = reactive({
      type: "其他",
      content: "",
      orderId: "",
    });

    // Quick categories
    const categories = [
      { key: "order", icon: "📦", label: "订单问题", keywords: "我的订单" },
      { key: "payment", icon: "💰", label: "支付问题", keywords: "怎么付款" },
      { key: "logistics", icon: "🚚", label: "物流发货", keywords: "什么时候发货" },
      { key: "after-sale", icon: "🔄", label: "退换货售后", keywords: "怎么退款" },
      { key: "coupon", icon: "🏷️", label: "优惠券/礼品卡", keywords: "优惠券怎么用" },
      { key: "points", icon: "⭐", label: "积分/抽奖", keywords: "积分有什么用" },
      { key: "address", icon: "📍", label: "账号与地址", keywords: "修改地址" },
    ];

    function formatTime(timeStr) {
      if (!timeStr) return "";
      try {
        const d = new Date(timeStr);
        const h = String(d.getHours()).padStart(2, "0");
        const m = String(d.getMinutes()).padStart(2, "0");
        return h + ":" + m;
      } catch (e) {
        return "";
      }
    }

    function formatMessage(text) {
      if (!text) return "";
      return text.replace(/\n/g, "<br>");
    }

    function scrollToBottom() {
      nextTick(() => {
        if (messagesRef.value) {
          messagesRef.value.scrollTop = messagesRef.value.scrollHeight;
        }
      });
    }

    // Select a quick category → send its keyword
    function selectCategory(cat) {
      activeCategory.value = cat.key;
      inputMessage.value = cat.keywords;
      sendMessage();
    }

    // Send message
    async function sendMessage() {
      const text = inputMessage.value.trim();
      if (!text || isTyping.value) return;

      // Add user message
      messages.value.push({
        role: "user",
        content: text,
        time: new Date().toISOString(),
      });
      inputMessage.value = "";
      scrollToBottom();

      // Simulate typing
      isTyping.value = true;
      activeCategory.value = null;

      try {
        // Add a small delay for realistic feel
        await new Promise((r) => setTimeout(r, 600 + Math.random() * 800));

        const data = await customerChat(text);
        const result = data.result || {};
        messages.value.push({
          role: "assistant",
          content: result.reply || "抱歉，我暂时无法回答这个问题。",
          suggestions: result.suggestions || [],
          type: result.type || "faq",
          time: new Date().toISOString(),
        });
      } catch (e) {
        messages.value.push({
          role: "assistant",
          content: "网络异常，请稍后重试。",
          suggestions: [],
          time: new Date().toISOString(),
        });
      } finally {
        isTyping.value = false;
        scrollToBottom();
      }
    }

    // Handle suggestion button click
    function handleSuggestion(action) {
      switch (action) {
        case "查看订单":
        case "查看待付款订单":
          router.push("/member/order");
          break;
        case "申请售后":
          router.push("/member/after-sale");
          break;
        case "评价商品":
          router.push("/member/review");
          break;
        case "查看优惠券":
          router.push("/member/coupon");
          break;
        case "绑定礼品卡":
          router.push("/member/coupon");
          break;
        case "去抽奖":
          router.push("/member/lottery");
          break;
        case "查看积分":
          router.push("/member/points");
          break;
        case "管理地址":
          router.push("/member/address");
          break;
        case "安全设置":
          router.push("/member/security");
          break;
        case "帮助中心":
          router.push("/help");
          break;
        case "去首页逛逛":
          router.push("/");
          break;
        case "提交工单":
        case "提交新工单":
          openTicketModal();
          break;
        case "我的订单":
          inputMessage.value = "我的订单";
          sendMessage();
          break;
        case "怎么退款":
          inputMessage.value = "怎么退款";
          sendMessage();
          break;
        case "优惠券怎么用":
          inputMessage.value = "优惠券怎么用";
          sendMessage();
          break;
        case "积分抽奖":
          inputMessage.value = "积分怎么用";
          sendMessage();
          break;
        case "查看工单":
          showTicketList();
          break;
        default:
          if (action.startsWith("/")) {
            router.push(action);
          } else {
            inputMessage.value = action;
            sendMessage();
          }
      }
    }

    // Ticket modal
    function openTicketModal() {
      ticketForm.type = "其他";
      ticketForm.content = "";
      ticketForm.orderId = "";
      ticketModalVisible.value = true;
    }

    function closeTicketModal() {
      ticketModalVisible.value = false;
    }

    async function submitTicket() {
      if (!ticketForm.type || !ticketForm.content.trim()) return;
      try {
        const data = await createCustomerTicket({
          type: ticketForm.type,
          content: ticketForm.content.trim(),
          orderId: ticketForm.orderId,
        });
        if (data.result) {
          Message({ type: "success", text: "工单提交成功，工单编号：" + data.result.id });
          closeTicketModal();

          // Add system message about ticket
          messages.value.push({
            role: "assistant",
            content: "您的工单（" + data.result.id + "）已提交成功，我们会尽快处理。您可以在“我的工单”中查看处理进度。",
            suggestions: ["查看工单"],
            time: new Date().toISOString(),
          });
          scrollToBottom();
        }
      } catch (e) {
        Message({ type: "error", text: "工单提交失败，请重试" });
      }
    }

    // Show ticket list as a chat message
    async function showTicketList() {
      try {
        const data = await getCustomerTickets();
        const items = data.result?.items || [];
        if (items.length === 0) {
          messages.value.push({
            role: "assistant",
            content: "您还没有提交过客服工单。如需提交工单，请发送无法解答的问题。",
            suggestions: [],
            time: new Date().toISOString(),
          });
          return;
        }
        let content = "以下是您的客服工单列表：\n\n";
        for (const t of items) {
          content += "工单编号：" + t.id + "\n";
          content += "问题类型：" + t.type + "\n";
          if (t.orderId) content += "关联订单：" + t.orderId + "\n";
          content += "处理状态：" + t.status + "\n";
          content += "创建时间：" + (t.createTime || "").substring(0, 19).replace("T", " ") + "\n";
          content += "------------------------\n";
        }
        messages.value.push({
          role: "assistant",
          content: content.trim(),
          suggestions: ["提交新工单"],
          time: new Date().toISOString(),
        });
        scrollToBottom();
      } catch (e) {
        messages.value.push({
          role: "assistant",
          content: "获取工单列表失败，请稍后重试。",
          suggestions: [],
          time: new Date().toISOString(),
        });
      }
    }

    // Load chat history on mount
    onMounted(async () => {
      try {
        const data = await getCustomerChatHistory();
        const items = data.result?.items || [];
        if (items.length > 0) {
          messages.value = items;
        } else {
          messages.value = [WELCOME_MESSAGE];
        }
      } catch (e) {
        messages.value = [WELCOME_MESSAGE];
      }
      scrollToBottom();
      if (inputRef.value) inputRef.value.focus();
    });

    return {
      messagesRef, inputRef, inputMessage, messages, isTyping, activeCategory,
      categories, ticketModalVisible, ticketForm,
      selectCategory, sendMessage, handleSuggestion,
      openTicketModal, closeTicketModal, submitTicket, showTicketList,
      formatTime, formatMessage,
    };
  },
};
</script>

<style scoped lang="less">
.page-customer-service {
  min-height: 600px;
  background: #f5f5f5;
}

.cs-container {
  display: flex;
  height: calc(100vh - 180px);
  min-height: 560px;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

/* ===== Sidebar ===== */
.cs-sidebar {
  width: 200px;
  background: #fafafa;
  border-right: 1px solid #eee;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  padding: 20px 16px 12px;
  margin: 0;
}

.category-list {
  list-style: none;
  margin: 0;
  padding: 0;
  flex: 1;
  overflow-y: auto;
}

.category-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  cursor: pointer;
  color: #555;
  font-size: 14px;
  transition: background 0.2s;
  border-left: 3px solid transparent;

  &:hover {
    background: #f0f0f0;
  }

  &.active {
    background: #fff;
    color: @xtxColor;
    border-left-color: @xtxColor;
    font-weight: 500;
  }
}

.cat-icon {
  font-size: 18px;
  line-height: 1;
}

.cat-label {
  line-height: 1.2;
}

.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid #eee;
}

.cs-notice {
  font-size: 11px;
  color: #aaa;
  line-height: 1.5;
  margin: 0;
}

/* ===== Main chat area ===== */
.cs-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
  flex-shrink: 0;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.header-status {
  font-size: 12px;
  color: #52c41a;
}

/* Messages area */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: #f9f9f9;
}

.message {
  display: flex;
  gap: 10px;
  max-width: 80%;
}

.message-user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-assistant {
  align-self: flex-start;
}

.msg-avatar {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
}

.bot-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: block;
}

.user-avatar-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: @xtxColor;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
}

.msg-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-width: 100%;
}

.msg-text {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
  white-space: pre-wrap;
}

.message-user .msg-text {
  background: @xtxColor;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.message-assistant .msg-text {
  background: #fff;
  color: #333;
  border: 1px solid #e8e8e8;
  border-bottom-left-radius: 4px;
}

.msg-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 4px;
}

.action-btn {
  padding: 5px 14px;
  border: 1px solid @xtxColor;
  border-radius: 16px;
  background: #fff;
  color: @xtxColor;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;

  &:hover {
    background: @xtxColor;
    color: #fff;
  }
}

.msg-time {
  font-size: 11px;
  color: #bbb;
  padding-left: 4px;
}

.message-user .msg-time {
  text-align: right;
}

/* Typing indicator */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
  align-items: center;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ccc;
  animation: typingBounce 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }
.typing-indicator span:nth-child(3) { animation-delay: 0s; }

@keyframes typingBounce {
  0%, 80%, 100% { transform: scale(0.6); }
  40% { transform: scale(1); }
}

/* Input area */
.chat-input-area {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #eee;
  background: #fff;
  flex-shrink: 0;
}

.input-wrapper {
  flex: 1;
  min-width: 0;
}

.chat-input {
  width: 100%;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 14px;
  line-height: 1.5;
  resize: none;
  outline: none;
  font-family: inherit;
  box-sizing: border-box;

  &:focus {
    border-color: @xtxColor;
  }

  &:disabled {
    background: #f5f5f5;
    cursor: not-allowed;
  }
}

.send-btn {
  padding: 0 24px;
  border: none;
  border-radius: 8px;
  background: @xtxColor;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  font-weight: 500;
  white-space: nowrap;
  height: 44px;
  align-self: flex-end;

  &:hover {
    opacity: 0.9;
  }

  &:disabled {
    background: #ddd;
    cursor: not-allowed;
  }
}

/* Ticket modal */
.ticket-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.ticket-modal {
  background: #fff;
  border-radius: 12px;
  width: 480px;
  max-width: 95vw;
  padding: 28px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.ticket-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 20px;
}

.ticket-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;

  label {
    font-size: 14px;
    color: #555;
    font-weight: 500;
  }
}

.ticket-select,
.ticket-input,
.ticket-textarea {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  font-family: inherit;

  &:focus {
    border-color: @xtxColor;
  }
}

.ticket-textarea {
  resize: vertical;
  min-height: 80px;
}

.ticket-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.btn-cancel {
  padding: 8px 24px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: #fff;
  color: #666;
  font-size: 14px;
  cursor: pointer;

  &:hover { border-color: #bbb; color: #333; }
}

.btn-submit {
  padding: 8px 24px;
  border: none;
  border-radius: 6px;
  background: @xtxColor;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  font-weight: 500;

  &:hover { opacity: 0.9; }
  &:disabled { background: #ddd; cursor: not-allowed; }
}
</style>

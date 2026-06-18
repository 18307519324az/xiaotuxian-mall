<template>
  <div class="member-address">
    <div class="member-nav">
      <span class="title">地址管理</span>
      <XtxButton @click="openEdit()" type="primary" size="small" style="margin-left: auto">添加地址</XtxButton>
    </div>
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="addressList.length === 0" class="empty">
      <img src="@/assets/images/empty.png" alt="" />
      <p>暂无收货地址</p>
    </div>
    <div v-else class="address-list">
      <div v-for="item in addressList" :key="item.id" class="address-item">
        <div class="info">
          <span class="name">{{ item.receiver }}</span>
          <span class="phone">{{ item.contact }}</span>
          <span v-if="item.isDefault === 0" class="default-badge">默认</span>
        </div>
        <div class="detail">
          {{ item.fullLocation }} {{ item.address }}
        </div>
        <div class="action">
          <a href="javascript:;" @click="setDefault(item)" v-if="item.isDefault !== 0">设为默认</a>
          <a href="javascript:;" @click="openEdit(item)">修改</a>
          <a href="javascript:;" @click="remove(item.id)">删除</a>
        </div>
      </div>
    </div>

    <!-- 复用 AddressEdit 组件 -->
    <AddressEdit ref="addressEditComp" @on-success="onAddressSuccess" />
  </div>
</template>

<script>
import { ref, onMounted } from "vue";
import { addAddress } from "@/api/order";
import { deleteAddress } from "@/api/order";
import AddressEdit from "@/views/member/pay/components/address-edit";
import Message from "@/components/library/Message";
import request from "@/utils/request";

export default {
  name: "MemberAddress",
  components: { AddressEdit },
  setup() {
    const addressList = ref([]);
    const loading = ref(true);
    const addressEditComp = ref(null);

    const loadData = async () => {
      loading.value = true;
      try {
        const data = await request("/member/address", "get");
        addressList.value = Array.isArray(data.result) ? data.result : [];
      } catch (e) {
        addressList.value = [];
      } finally {
        loading.value = false;
      }
    };

    onMounted(loadData);

    const openEdit = (address) => {
      addressEditComp.value?.open(address || null);
    };

    const onAddressSuccess = () => {
      loadData();
    };

    const setDefault = async (item) => {
      try {
        await request("/member/address/" + item.id, "put", {
          ...item,
          isDefault: 0,
        });
        Message({ type: "success", text: "已设为默认地址" });
        loadData();
      } catch (e) {
        Message({ type: "error", text: "设置失败" });
      }
    };

    const remove = async (id) => {
      try {
        await deleteAddress(id);
        Message({ type: "success", text: "删除成功" });
        loadData();
      } catch (e) {
        Message({ type: "error", text: "删除失败" });
      }
    };

    return { addressList, loading, addressEditComp, openEdit, onAddressSuccess, setDefault, remove };
  },
};
</script>

<style scoped lang="less">
.member-address {
  background: #fff;
  min-height: 400px;
  .member-nav {
    padding: 20px;
    border-bottom: 1px solid #f5f5f5;
    display: flex;
    align-items: center;
    .title {
      font-size: 18px;
      font-weight: 500;
    }
  }
  .address-list {
    padding: 20px;
    .address-item {
      padding: 15px 20px;
      border-bottom: 1px solid #f5f5f5;
      .info {
        display: flex;
        align-items: center;
        .name {
          font-size: 16px;
          font-weight: 500;
        }
        .phone {
          color: #999;
          margin-left: 20px;
        }
        .default-badge {
          background: @xtxColor;
          color: #fff;
          font-size: 12px;
          padding: 2px 8px;
          border-radius: 2px;
          margin-left: 10px;
        }
      }
      .detail {
        color: #666;
        font-size: 14px;
        margin-top: 8px;
      }
      .action {
        margin-top: 10px;
        a {
          color: #999;
          margin-right: 15px;
          font-size: 13px;
          &:hover {
            color: @xtxColor;
          }
        }
      }
    }
  }
  .loading {
    text-align: center;
    padding: 100px 0;
    color: #999;
  }
  .empty {
    text-align: center;
    padding: 80px 0;
    img {
      width: 120px;
    }
    p {
      color: #999;
      margin-top: 15px;
    }
  }
}
</style>

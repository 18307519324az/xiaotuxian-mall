/**
 * format.js — 前端文本格式化工具函数
 *
 * formatAttrsText(val): 防御性解析商品规格文本
 *   - Array 类型: [{name, valueName}] → "名称：值"
 *   - String JSON: "[{name, valueName}]" → parse → "名称：值"
 *   - String 普通: 直接返回
 *   - Object: {name, valueName} → "名称：值"
 *   - null/undefined: 返回 ''
 */

export const formatAttrsText = (val) => {
  if (!val) return '';
  if (Array.isArray(val)) {
    return val
      .map((item) => {
        const name = item.name || '';
        const value = item.valueName || item.value || '';
        return name ? `${name}：${value}` : value;
      })
      .filter(Boolean)
      .join(' ');
  }
  if (typeof val === 'string') {
    const text = val.trim();
    if (text.startsWith('[{') || text.startsWith('[')) {
      try {
        return formatAttrsText(JSON.parse(text));
      } catch (e) {
        return val;
      }
    }
    return val;
  }
  if (typeof val === 'object') {
    const name = val.name || '';
    const value = val.valueName || val.value || '';
    return name ? `${name}：${value}` : value;
  }
  return String(val);
};

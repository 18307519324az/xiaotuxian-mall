#!/usr/bin/env node

const { chromium } = require("playwright");

const FRONTEND_URL = process.env.FRONTEND_URL || "http://localhost:8086/#/member/checkout";
const AUTH_TOKEN = process.env.AUTH_TOKEN || "";
const STORAGE_KEY = process.env.STORAGE_KEY || "erabbit-client-pc-store";

async function main() {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  const checks = [];
  const payloads = [];

  page.on("request", (request) => {
    if (request.url().includes("/api/member/order")) {
      payloads.push({
        url: request.url(),
        body: request.postData(),
      });
    }
  });

  if (AUTH_TOKEN) {
    await page.addInitScript(({ storageKey, token }) => {
      localStorage.setItem(
        storageKey,
        JSON.stringify({
          user: {
            profile: { token },
          },
        })
      );
    }, { storageKey: STORAGE_KEY, token: AUTH_TOKEN });
  }

  await page.goto(FRONTEND_URL, { waitUntil: "networkidle" });

  const text = await page.locator("body").innerText();
  const amountSummary = await page.locator(".total").innerText().catch(() => "");

  checks.push(["coupon section visible", text.includes("优惠券")]);
  checks.push(["gift-card section visible", text.includes("礼品卡")]);
  checks.push(["amount summary visible", amountSummary.length > 0]);
  checks.push(["no NaN text", !text.includes("NaN")]);
  checks.push(["no undefined text", !text.includes("undefined")]);
  checks.push(["no null text", !text.includes("null")]);

  console.log(JSON.stringify({ checks, payloads }, null, 2));

  await browser.close();

  if (checks.some(([, pass]) => !pass)) {
    process.exit(1);
  }
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});

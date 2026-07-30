# -*- coding: utf-8 -*-
"""
微光守护演示站 - 手机模拟器截图脚本
功能：使用 Playwright 打开演示站 HTML，依次切换各功能模块，
      截取 phone-frame 手机模拟器元素的画面，保存为 PNG 文件。

使用方式：python screenshot_phone.py
输出目录：f:\java\weiguangplus\weiguang123\ppt_workspace\screenshots\
"""

import asyncio
from pathlib import Path
from playwright.async_api import async_playwright


async def main():
    # ── 路径配置 ──
    html_path = Path(
        r"f:\java\weiguangplus\weiguang123\参赛材料\微光科技_微光守护_演示站.html"
    ).resolve()
    output_dir = Path(
        r"f:\java\weiguangplus\weiguang123\ppt_workspace\screenshots"
    )
    output_dir.mkdir(parents=True, exist_ok=True)

    # ── 截图任务定义：页面名称 -> 输出文件名 ──
    modules = [
        ("home",    "phone_01.png"),   # 首页/主界面
        ("fire",    "phone_02.png"),   # 火灾预警模块
        ("gas",     "phone_03.png"),   # 燃气泄漏预警模块
        ("gesture", "phone_04.png"),   # 手势识别模块
        ("knock",   "phone_05.png"),   # 敲门检测模块
    ]

    async with async_playwright() as p:
        # 启动 Chromium 浏览器（使用足够大的视口确保手机模拟器完整显示）
        browser = await p.chromium.launch()
        page = await browser.new_page(viewport={"width": 1920, "height": 1080})

        # 打开 HTML 文件
        file_url = f"file:///{html_path.as_posix()}"
        print(f"🌐 正在打开: {file_url}")
        await page.goto(file_url, wait_until="networkidle")

        # 等待手机框架元素加载完成
        await page.wait_for_selector(".phone-frame", state="visible", timeout=15000)
        await page.wait_for_timeout(1500)  # 等待动画完成

        # 锁定 3D 旋转为 0（避免 requestAnimationFrame 产生微小偏移）
        await page.evaluate("""
            // 将 3D 场景的 transform 强制锁定为 0 度
            const scene = document.getElementById('phone3dScene');
            if (scene) {
                scene.style.transform = 'rotateX(0deg) rotateY(0deg)';
                // 禁止后续动画覆盖
                scene.style.transition = 'none';
            }
            // 停止所有正在播放的音频
            if (typeof stopAllSounds === 'function') stopAllSounds();
            if (typeof stopEmergency === 'function') stopEmergency();
        """)

        for page_name, filename in modules:
            print(f"📸 正在截取: {page_name} -> {filename}")

            # 切换到目标模块页面
            await page.evaluate(f"goPage('{page_name}')")
            await page.wait_for_timeout(600)  # 等待页面切换动画

            # 确保手机内容区滚动到顶部
            await page.evaluate("""
                const content = document.getElementById('phoneContent');
                if (content) content.scrollTop = 0;
            """)
            await page.wait_for_timeout(300)

            # 再次确保 3D 旋转为 0（切换页面不会影响 3D 旋转，但保险起见）
            await page.evaluate("""
                const scene = document.getElementById('phone3dScene');
                if (scene) {
                    scene.style.transform = 'rotateX(0deg) rotateY(0deg)';
                }
            """)

            # 截取 phone-frame 元素
            phone_frame = page.locator(".phone-frame")
            output_path = output_dir / filename
            await phone_frame.screenshot(path=str(output_path))

            file_size = output_path.stat().st_size
            print(f"  ✅ 已保存: {output_path} ({file_size / 1024:.1f} KB)")

        await browser.close()
        print(f"\n🎉 全部完成！共截取 {len(modules)} 张图片，保存在: {output_dir}")


if __name__ == "__main__":
    asyncio.run(main())
/**
 * FEN-SAFE 网页截图工具 (Playwright版)
 * 用于专利支撑材料文档的图片采集
 */
const { chromium } = require('playwright');
const path = require('path');

const SCREENSHOT_DIR = path.join(__dirname, 'fensafe-algorithm', '1、', 'screenshots');
const DEMO_HTML = path.resolve(__dirname, 'fensafe-algorithm', 'demo', 'fensafe-demo.html');
const DEMO_3D_HTML = path.resolve(__dirname, 'fensafe-algorithm', 'demo', 'fensafe-3d-demo.html');

async function screenshot(page, filePath, options = {}) {
    const { fullPage = false, scrollBefore = 0, delay = 1000, clickSelector = null } = options;
    console.log(`  截图: ${path.basename(filePath)}`);
    
    if (scrollBefore > 0) await page.evaluate(y => window.scrollBy(0, y), scrollBefore);
    if (delay > 0) await page.waitForTimeout(delay);
    
    if (clickSelector) {
        try {
            const el = page.locator(clickSelector).first();
            if (await el.count() > 0) {
                await el.click();
                await page.waitForTimeout(3000); // 等待操作完成
            }
        } catch(e) {
            console.log(`    选择器 ${clickSelector} 未找到，跳过`);
        }
    }
    
    await page.screenshot({ 
        path: filePath, 
        fullPage
    });
    console.log(`    OK: ${filePath}`);
}

async function main() {
    console.log('=== FEN-SAFE 网页截图工具 (Playwright) ===\n');
    
    const browser = await chromium.launch({ headless: true });
    const context = await browser.newContext({
        viewport: { width: 1920, height: 1080 },
        deviceScaleFactor: 1
    });

    try {
        // ====== 截图1: 主演示页面全貌 ======
        console.log('[1/5] 主演示页面全貌...');
        let page = await context.newPage();
        await page.goto('file:///' + DEMO_HTML.replace(/\\/g, '/'), { waitUntil: 'networkidle', timeout: 30000 });
        await page.waitForTimeout(2000);
        await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'shot_01_main_demo.png'), fullPage: true });
        console.log('    OK: shot_01_main_demo.png');
        await page.close();

        // ====== 截图2: S盒区域 ======
        console.log('[2/5] S盒可视化区域...');
        page = await context.newPage();
        await page.goto('file:///' + DEMO_HTML.replace(/\\/g, '/'), { waitUntil: 'networkidle', timeout: 30000 });
        await page.evaluate(() => window.scrollTo(0, 800));
        await page.waitForTimeout(1000);
        await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'shot_02_sbox_visual.png') });
        console.log('    OK: shot_02_sbox_visual.png');
        await page.close();

        // ====== 截图3: 雪崩测试区域 (尝试点击运行按钮) ======
        console.log('[3/5] 雪崩效应测试区域...');
        page = await context.newPage();
        await page.goto('file:///' + DEMO_HTML.replace(/\\/g, '/'), { waitUntil: 'networkidle', timeout: 30000 });
        await page.evaluate(() => window.scrollTo(0, 2000));
        await page.waitForTimeout(1000);
        
        // 尝试找雪崩测试按钮并点击
        try {
            const avalancheBtn = page.locator('#runAvalancheTest, [id*="avalanche"], [onclick*="avalanche"], button:has-text("雪崩")').first();
            if (await avalancheBtn.count() > 0) {
                await avalancheBtn.click();
                console.log('    已点击雪崩测试按钮，等待结果...');
                await page.waitForTimeout(5000);
            }
        } catch(e) {
            console.log('    未找到雪崩按钮，直接截图');
        }
        
        await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'shot_03_avalanche.png') });
        console.log('    OK: shot_03_avalanche.png');
        await page.close();

        // ====== 截图4: 算法对比表格 ======
        console.log('[4/5] 算法对比表格区域...');
        page = await context.newPage();
        await page.goto('file:///' + DEMO_HTML.replace(/\\/g, '/'), { waitUntil: 'networkidle', timeout: 30000 });
        await page.evaluate(() => window.scrollTo(0, 3500));
        await page.waitForTimeout(1000);
        await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'shot_04_comparison.png') });
        console.log('    OK: shot_04_comparison.png');
        await page.close();

        // ====== 截图5: 3D演示页面 ======
        console.log('[5/5] 3D演示页面...');
        page = await context.newPage();
        await page.goto('file:///' + DEMO_3D_HTML.replace(/\\/g, '/'), { waitUntil: 'networkidle', timeout: 30000 });
        // 3D场景需要更多时间渲染
        console.log('    等待Three.js 3D场景加载...');
        await page.waitForTimeout(5000);
        await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'shot_05_3d_demo.png') });
        console.log('    OK: shot_05_3d_demo.png');
        await page.close();

        console.log('\n=== 全部5张截图完成! ===');
        console.log(`输出目录: ${SCREENSHOT_DIR}`);

    } catch(err) {
        console.error('截图失败:', err.message);
        console.error(err.stack);
    } finally {
        await browser.close();
    }
}

main().catch(console.error);

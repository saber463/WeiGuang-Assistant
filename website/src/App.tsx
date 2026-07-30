import { useState, useEffect, useRef, useCallback } from 'react'

// ═══════════════════════════════════════════════════════════════════════════════
// Hooks
// ═══════════════════════════════════════════════════════════════════════════════

/** 滚动触发动画 Hook */
function useScrollReveal(threshold = 0.15) {
  const ref = useRef<HTMLDivElement>(null)
  const [visible, setVisible] = useState(false)

  useEffect(() => {
    const el = ref.current
    if (!el) return
    const observer = new IntersectionObserver(
      ([entry]) => { if (entry.isIntersecting) setVisible(true) },
      { threshold }
    )
    observer.observe(el)
    return () => observer.disconnect()
  }, [threshold])

  return { ref, visible }
}

/** 数字递增动画 Hook */
function useCountUp(end: number, duration = 2000, start = false) {
  const [count, setCount] = useState(0)
  useEffect(() => {
    if (!start) return
    let startTime: number | null = null
    const animate = (timestamp: number) => {
      if (!startTime) startTime = timestamp
      const progress = Math.min((timestamp - startTime) / duration, 1)
      setCount(Math.floor(progress * end))
      if (progress < 1) requestAnimationFrame(animate)
    }
    requestAnimationFrame(animate)
  }, [end, duration, start])
  return count
}

// ═══════════════════════════════════════════════════════════════════════════════
// 导航栏
// ═══════════════════════════════════════════════════════════════════════════════
function Navbar() {
  const [scrolled, setScrolled] = useState(false)
  const [mobileOpen, setMobileOpen] = useState(false)

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 20)
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [])

  const links = [
    { label: '首页', href: '#hero' },
    { label: '功能', href: '#features' },
    { label: 'SOS', href: '#sos' },
    { label: '技术', href: '#tech' },
    { label: '团队', href: '#team' },
  ]

  return (
    <nav className={`fixed top-0 left-0 right-0 z-50 transition-all duration-500 ${
      scrolled ? 'bg-white/95 backdrop-blur-xl shadow-lg shadow-orange-500/5' : 'bg-transparent'
    }`}>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <a href="#hero" className="flex items-center gap-2.5 group">
            <div className={`w-9 h-9 rounded-xl flex items-center justify-center text-lg transition-all duration-500 ${
              scrolled ? 'bg-gradient-to-br from-orange-500 to-red-500' : 'bg-white/20'
            }`}>
              🌟
            </div>
            <span className={`font-bold text-lg transition-colors duration-500 ${
              scrolled ? 'text-gray-900' : 'text-white'
            }`}>
              微光<span className="text-orange-500">同行</span>
            </span>
          </a>

          <div className="hidden md:flex items-center gap-1">
            {links.map(link => (
              <a
                key={link.href}
                href={link.href}
                className={`px-4 py-2 rounded-full text-sm font-medium transition-all duration-300 ${
                  scrolled
                    ? 'text-gray-600 hover:text-orange-500 hover:bg-orange-50'
                    : 'text-white/85 hover:text-white hover:bg-white/10'
                }`}
              >
                {link.label}
              </a>
            ))}
          </div>

          <button className="md:hidden p-2" onClick={() => setMobileOpen(!mobileOpen)}>
            <div className={`w-6 h-0.5 mb-1.5 transition-all ${scrolled ? 'bg-gray-600' : 'bg-white'}`} />
            <div className={`w-6 h-0.5 mb-1.5 transition-all ${scrolled ? 'bg-gray-600' : 'bg-white'}`} />
            <div className={`w-6 h-0.5 transition-all ${scrolled ? 'bg-gray-600' : 'bg-white'}`} />
          </button>
        </div>
      </div>

      {mobileOpen && (
        <div className="md:hidden bg-white/95 backdrop-blur-xl border-t border-gray-100">
          <div className="px-4 py-3 space-y-1">
            {links.map(link => (
              <a key={link.href} href={link.href} onClick={() => setMobileOpen(false)}
                className="block px-4 py-3 rounded-xl text-gray-600 hover:text-orange-500 hover:bg-orange-50 transition-all">
                {link.label}
              </a>
            ))}
          </div>
        </div>
      )}
    </nav>
  )
}

// ═══════════════════════════════════════════════════════════════════════════════
// Hero 区块 — 粒子背景 + 浮动手势
// ═══════════════════════════════════════════════════════════════════════════════
function HeroSection() {
  return (
    <section id="hero" className="relative min-h-screen flex items-center justify-center overflow-hidden"
      style={{ background: 'linear-gradient(135deg, #0f0c29 0%, #1a1a3e 25%, #24243e 50%, #e94560 85%, #f97316 100%)' }}
    >
      {/* 动态粒子 */}
      <div className="absolute inset-0 overflow-hidden">
        {[...Array(30)].map((_, i) => (
          <div key={i} className="absolute rounded-full bg-white/10"
            style={{
              width: `${Math.random() * 6 + 2}px`,
              height: `${Math.random() * 6 + 2}px`,
              left: `${Math.random() * 100}%`,
              top: `${Math.random() * 100}%`,
              animation: `float ${Math.random() * 6 + 4}s ease-in-out ${Math.random() * 2}s infinite`,
              opacity: Math.random() * 0.5 + 0.1,
            }}
          />
        ))}
      </div>

      {/* 光晕 */}
      <div className="absolute top-20 left-10 w-72 h-72 bg-orange-500/20 rounded-full blur-[100px]" />
      <div className="absolute bottom-20 right-10 w-96 h-96 bg-red-500/15 rounded-full blur-[120px]" />
      <div className="absolute top-1/3 right-1/4 w-64 h-64 bg-yellow-500/10 rounded-full blur-[80px]" />

      <div className="relative z-10 text-center px-4 max-w-5xl mx-auto">
        {/* 顶部标签 */}
        <div className="animate-fade-in-up mb-8">
          <div className="inline-flex items-center gap-2 glass rounded-full px-5 py-2">
            <span className="w-2 h-2 bg-green-400 rounded-full animate-pulse-soft" />
            <span className="text-white/80 text-sm font-medium">大学生创新创业大赛 · 红旅赛道</span>
          </div>
        </div>

        {/* 主标题 */}
        <h1 className="animate-fade-in-up delay-1 text-6xl md:text-8xl font-black text-white mb-6 leading-tight tracking-tight">
          微光<span className="text-gradient">同行</span>
        </h1>

        <p className="animate-fade-in-up delay-2 text-xl md:text-2xl text-white/80 mb-3 font-light">
          智能助残服务平台
        </p>
        <p className="animate-fade-in-up delay-3 text-base md:text-lg text-white/50 mb-12 max-w-xl mx-auto leading-relaxed">
          以AI之手，搭建听障人士与世界沟通的桥梁
        </p>

        {/* 按钮组 */}
        <div className="animate-fade-in-up delay-4 flex flex-col sm:flex-row gap-4 justify-center mb-16">
          <a href="#features"
            className="group px-8 py-3.5 bg-gradient-to-r from-orange-500 to-red-500 text-white rounded-full font-semibold shadow-lg shadow-orange-500/25 hover:shadow-xl hover:shadow-orange-500/40 hover:scale-105 transition-all duration-300">
            <span className="flex items-center gap-2">
              探索功能
              <svg className="w-4 h-4 group-hover:translate-x-1 transition-transform" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 8l4 4m0 0l-4 4m4-4H3" />
              </svg>
            </span>
          </a>
          <a href="#sos"
            className="px-8 py-3.5 glass rounded-full font-semibold text-white hover:bg-white/20 hover:scale-105 transition-all duration-300">
            SOS 演示
          </a>
        </div>

        {/* 统计数据条 */}
        <div className="animate-fade-in-up delay-5 flex flex-wrap justify-center gap-8 md:gap-16">
          {[
            { value: '11', label: '手势识别' },
            { value: '80+', label: '商品词库' },
            { value: '100%', label: '端侧推理' },
            { value: '3', label: '赛事参赛' },
          ].map((stat, i) => (
            <div key={i} className="text-center">
              <div className="text-2xl md:text-3xl font-black text-white mb-1">{stat.value}</div>
              <div className="text-white/40 text-xs md:text-sm">{stat.label}</div>
            </div>
          ))}
        </div>
      </div>

      {/* 滚动指示器 */}
      <div className="absolute bottom-8 left-1/2 -translate-x-1/2 animate-fade-in-up delay-6">
        <div className="w-6 h-10 border-2 border-white/20 rounded-full flex justify-center">
          <div className="w-1 h-2.5 bg-orange-400 rounded-full mt-2 animate-bounce" />
        </div>
      </div>
    </section>
  )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 功能卡片区块
// ═══════════════════════════════════════════════════════════════════════════════
function FeaturesSection() {
  const { ref, visible } = useScrollReveal()

  const features = [
    { icon: '🆘', title: 'SOS 紧急求助', desc: '握拳/张开手掌手势即触发，GPS+气压计精准定位到楼层，FCM实时推送家属端', gradient: 'from-red-500 to-orange-500', bg: 'bg-red-50', border: 'border-red-200' },
    { icon: '🤟', title: '双向手语识别', desc: 'MediaPipe 21点关键点 + 向量数据库余弦匹配 + 序列检测，规则+ML双引擎', gradient: 'from-blue-500 to-cyan-500', bg: 'bg-blue-50', border: 'border-blue-200' },
    { icon: '📷', title: 'OCR 商品识别', desc: '实时摄像头扫描商品包装，全国统一市场价匹配，支持80+常见日用品关键词', gradient: 'from-green-500 to-emerald-500', bg: 'bg-green-50', border: 'border-green-200' },
    { icon: '🔊', title: 'TTS 语音播报', desc: '手势识别结果自动转语音播报，SOS手势10秒防重复，支持离线语音合成', gradient: 'from-purple-500 to-pink-500', bg: 'bg-purple-50', border: 'border-purple-200' },
    { icon: '📱', title: '家属端联动', desc: '一对一绑定码连接，Android原生APP + 微信小程序双端覆盖，实时位置共享', gradient: 'from-yellow-500 to-amber-500', bg: 'bg-yellow-50', border: 'border-yellow-200' },
    { icon: '🔒', title: '隐私安全保护', desc: '端到端加密传输，数据不出设备，AI推理全本地化，符合个人信息保护法规', gradient: 'from-indigo-500 to-violet-500', bg: 'bg-indigo-50', border: 'border-indigo-200' },
  ]

  return (
    <section id="features" className="py-28 bg-gradient-to-b from-gray-50 to-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div ref={ref} className={`scroll-reveal ${visible ? 'visible' : ''}`}>
          <div className="text-center mb-16">
            <div className="inline-flex items-center gap-2 px-4 py-1.5 bg-orange-50 rounded-full mb-6">
              <span className="w-2 h-2 bg-orange-500 rounded-full" />
              <span className="text-orange-600 text-sm font-medium">核心能力</span>
            </div>
            <h2 className="text-4xl md:text-5xl font-black text-gray-900 mb-4">
              核心<span className="text-gradient">功能</span>
            </h2>
            <p className="text-gray-500 text-lg max-w-2xl mx-auto">
              六大核心功能模块，全方位守护听障人士的日常生活安全与便利
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {features.map((feature, index) => {
            const { ref: cardRef, visible: cardVisible } = useScrollReveal(0.1)
            return (
              <div key={index} ref={cardRef} className={`scroll-reveal ${cardVisible ? 'visible' : ''}`}
                style={{ transitionDelay: `${index * 0.1}s` }}>
                <div className={`card-hover group relative bg-white rounded-2xl p-8 border ${feature.border} hover:border-transparent`}>
                  <div className="absolute inset-0 rounded-2xl opacity-0 group-hover:opacity-100 transition-opacity duration-500"
                    style={{ background: `linear-gradient(135deg, ${feature.bg}, transparent)` }} />
                  <div className="relative">
                    <div className={`w-14 h-14 ${feature.bg} rounded-2xl flex items-center justify-center text-2xl mb-5 group-hover:scale-110 group-hover:rotate-3 transition-all duration-500`}>
                      {feature.icon}
                    </div>
                    <h3 className="text-xl font-bold text-gray-900 mb-3">{feature.title}</h3>
                    <p className="text-gray-500 leading-relaxed text-sm">{feature.desc}</p>
                    <div className={`mt-5 h-1 w-12 rounded-full bg-gradient-to-r ${feature.gradient} opacity-0 group-hover:opacity-100 group-hover:w-20 transition-all duration-500`} />
                  </div>
                </div>
              </div>
            )
          })}
        </div>
      </div>
    </section>
  )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 数据统计面板
// ═══════════════════════════════════════════════════════════════════════════════
function StatsSection() {
  const { ref, visible } = useScrollReveal()
  const stats = [
    { end: 11, label: '手势识别类型', suffix: '种' },
    { end: 86, label: '商品关键词库', suffix: '+' },
    { end: 100, label: '端侧推理', suffix: '%' },
    { end: 1000, label: '合成训练样本', suffix: '+' },
  ]

  return (
    <section className="py-20 bg-gradient-to-r from-orange-500 via-red-500 to-orange-500">
      <div ref={ref} className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
          {stats.map((stat, i) => (
            <StatItem key={i} end={stat.end} label={stat.label} suffix={stat.suffix} start={visible} delay={i * 200} />
          ))}
        </div>
      </div>
    </section>
  )
}

function StatItem({ end, label, suffix, start, delay }: { end: number; label: string; suffix: string; start: boolean; delay: number }) {
  const [shouldCount, setShouldCount] = useState(false)
  useEffect(() => {
    if (start) {
      const timer = setTimeout(() => setShouldCount(true), delay)
      return () => clearTimeout(timer)
    }
  }, [start, delay])
  const count = useCountUp(end, 2000, shouldCount)
  return (
    <div className="text-center text-white">
      <div className="text-4xl md:text-5xl font-black mb-2">
        {count}{suffix}
      </div>
      <div className="text-white/70 text-sm">{label}</div>
    </div>
  )
}

// ═══════════════════════════════════════════════════════════════════════════════
// SOS 演示区块
// ═══════════════════════════════════════════════════════════════════════════════
function SosSection() {
  const { ref, visible } = useScrollReveal()
  const [sosState, setSosState] = useState<'idle' | 'detecting' | 'alerting' | 'done'>('idle')

  const triggerSOS = () => {
    if (sosState !== 'idle') return
    setSosState('detecting')
    setTimeout(() => setSosState('alerting'), 1500)
    setTimeout(() => setSosState('done'), 4000)
    setTimeout(() => setSosState('idle'), 7000)
  }

  const steps = [
    { step: '01', title: '手势触发', desc: '握拳✊或手掌张开🖐️手势被摄像头瞬间识别', icon: '✊' },
    { step: '02', title: '精确定位', desc: 'GPS + 气压计双重定位，精确到具体楼层', icon: '📍' },
    { step: '03', title: '即时推送', desc: 'FCM Firebase 实时推送求助信息到家属端', icon: '📲' },
    { step: '04', title: '家属响应', desc: '家属端接收位置信息，导航前往救援', icon: '🏃' },
  ]

  return (
    <section id="sos" className="py-28 bg-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div ref={ref} className={`scroll-reveal ${visible ? 'visible' : ''}`}>
          <div className="text-center mb-16">
            <div className="inline-flex items-center gap-2 px-4 py-1.5 bg-red-50 rounded-full mb-6">
              <span className="w-2 h-2 bg-red-500 rounded-full animate-pulse" />
              <span className="text-red-600 text-sm font-medium">安全守护</span>
            </div>
            <h2 className="text-4xl md:text-5xl font-black text-gray-900 mb-4">
              <span className="text-gradient">SOS</span> 紧急求助
            </h2>
            <p className="text-gray-500 text-lg">手势触发，秒级响应，精准定位到楼层</p>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-16 items-center">
          {/* 左侧流程 */}
          <div className="space-y-6">
            {steps.map((item, i) => {
              const { ref: stepRef, visible: stepVisible } = useScrollReveal()
              return (
                <div key={i} ref={stepRef} className={`scroll-reveal flex gap-5 items-start group ${stepVisible ? 'visible' : ''}`}
                  style={{ transitionDelay: `${i * 0.15}s` }}>
                  <div className="relative flex-shrink-0">
                    <div className="w-14 h-14 bg-red-50 rounded-2xl flex items-center justify-center text-xl group-hover:bg-red-100 group-hover:scale-110 transition-all duration-300">
                      {item.icon}
                    </div>
                    {i < steps.length - 1 && (
                      <div className="absolute top-14 left-7 w-0.5 h-8 bg-red-100 group-hover:bg-red-200 transition-colors" />
                    )}
                  </div>
                  <div className="pt-1">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="text-xs font-black text-red-500 bg-red-50 px-2.5 py-0.5 rounded-full">{item.step}</span>
                      <h4 className="font-bold text-gray-900">{item.title}</h4>
                    </div>
                    <p className="text-gray-500 text-sm leading-relaxed">{item.desc}</p>
                  </div>
                </div>
              )
            })}
          </div>

          {/* 右侧演示区 */}
          <div className="relative">
            {/* 背景光晕 */}
            <div className="absolute inset-0 bg-gradient-to-br from-red-500/10 to-orange-500/10 rounded-[40px] blur-2xl" />
            <div className="relative bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900 rounded-[40px] p-8 md:p-10 text-center border border-gray-700/50">
              <div className="mb-6">
                <span className="inline-flex items-center gap-2 px-4 py-1.5 bg-white/5 rounded-full text-white/50 text-xs">
                  <span className="w-1.5 h-1.5 bg-green-400 rounded-full animate-pulse" />
                  Demo 演示
                </span>
              </div>

              {/* 模拟手机 */}
              <div className="w-52 h-[420px] bg-black rounded-[32px] mx-auto border-[3px] border-gray-600 overflow-hidden shadow-2xl relative">
                <div className="h-8 bg-black flex items-center justify-between px-6 text-white/30 text-xs">
                  <span>9:41</span>
                  <div className="flex gap-1">
                    <span className="text-[10px]">📶</span>
                    <span className="text-[10px]">🔋</span>
                  </div>
                </div>

                <div className="p-5 text-center h-[calc(100%-2rem)] flex flex-col justify-center">
                  {sosState === 'idle' && (
                    <div className="animate-scale-in">
                      <div className="text-6xl mb-4 animate-float">✊</div>
                      <p className="text-white/50 text-sm mb-2">握拳触发 SOS 求助</p>
                      <p className="text-white/20 text-xs mb-6">或手掌张开</p>
                      <button onClick={triggerSOS}
                        className="px-6 py-2.5 bg-red-500 hover:bg-red-600 text-white rounded-full text-sm font-semibold transition-all shadow-lg shadow-red-500/25">
                        模拟触发
                      </button>
                    </div>
                  )}

                  {sosState === 'detecting' && (
                    <div className="animate-scale-in">
                      <div className="relative inline-block">
                        <div className="text-6xl mb-4 animate-bounce">🔍</div>
                        <div className="absolute inset-0 bg-yellow-400/20 rounded-full blur-xl animate-pulse" />
                      </div>
                      <p className="text-yellow-400 text-sm font-semibold">检测到 SOS 手势</p>
                      <p className="text-white/30 text-xs mt-2">正在获取位置信息...</p>
                      <div className="mt-4 w-32 h-1 bg-gray-800 rounded-full mx-auto overflow-hidden">
                        <div className="h-full bg-yellow-400 rounded-full animate-shimmer" style={{ width: '60%' }} />
                      </div>
                    </div>
                  )}

                  {sosState === 'alerting' && (
                    <div className="animate-scale-in">
                      <div className="relative inline-block">
                        <div className="text-6xl mb-4 animate-pulse">🚨</div>
                        <div className="absolute inset-0 bg-red-500/30 rounded-full blur-2xl animate-pulse" />
                      </div>
                      <p className="text-red-400 text-sm font-bold mb-3">SOS 已触发！</p>
                      <div className="bg-red-500/10 rounded-xl p-3 text-left space-y-1.5">
                        <p className="text-white/60 text-xs">📍 成都市武侯区</p>
                        <p className="text-white/60 text-xs">🏢 望江大厦 5层</p>
                        <p className="text-white/60 text-xs">📱 已通知家属端</p>
                        <p className="text-white/60 text-xs">⏱️ 预计 3 分钟内响应</p>
                      </div>
                    </div>
                  )}

                  {sosState === 'done' && (
                    <div className="animate-scale-in">
                      <div className="text-6xl mb-4">✅</div>
                      <p className="text-green-400 text-sm font-semibold mb-1">家属已确认</p>
                      <p className="text-white/30 text-xs">救援正在进行中...</p>
                      <div className="mt-4 flex justify-center gap-1">
                        {[1, 2, 3].map(i => (
                          <div key={i} className="w-2 h-2 bg-green-500 rounded-full animate-pulse-soft"
                            style={{ animationDelay: `${i * 0.3}s` }} />
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              </div>

              <p className="text-white/20 text-xs mt-4">
                * 实际使用中通过摄像头自动识别手势，无需手动点击
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 技术架构区块
// ═══════════════════════════════════════════════════════════════════════════════
function TechSection() {
  const { ref, visible } = useScrollReveal()

  const techs = [
    {
      category: 'AI 引擎', icon: '🧠',
      items: [
        { name: 'MediaPipe Hands', desc: '21点手部追踪' },
        { name: 'YOLO 物体检测', desc: '实时商品识别' },
        { name: 'TFLite 推理', desc: '端侧深度学习' },
        { name: '向量数据库', desc: '余弦相似度匹配' },
      ],
      color: 'border-blue-500', bg: 'from-blue-500/5 to-blue-500/10',
    },
    {
      category: '移动端', icon: '📱',
      items: [
        { name: 'Android Jetpack', desc: '现代架构组件' },
        { name: 'CameraX', desc: '相机管道分析' },
        { name: 'Compose UI', desc: '声明式界面' },
        { name: '离线TTS', desc: '本地语音合成' },
      ],
      color: 'border-green-500', bg: 'from-green-500/5 to-green-500/10',
    },
    {
      category: '云服务', icon: '☁️',
      items: [
        { name: 'Firebase FCM', desc: '实时消息推送' },
        { name: 'GPS 定位', desc: '精准位置获取' },
        { name: '气压计', desc: '楼层高度计算' },
        { name: '阿里云 ECS', desc: '网站托管服务' },
      ],
      color: 'border-purple-500', bg: 'from-purple-500/5 to-purple-500/10',
    },
    {
      category: '数据安全', icon: '🔒',
      items: [
        { name: '端到端加密', desc: '传输层安全防护' },
        { name: '端侧推理', desc: '数据不出设备' },
        { name: '本地存储', desc: '无云端上传' },
        { name: '隐私合规', desc: '个人信息保护法' },
      ],
      color: 'border-orange-500', bg: 'from-orange-500/5 to-orange-500/10',
    },
  ]

  return (
    <section id="tech" className="py-28 bg-gradient-to-b from-white to-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div ref={ref} className={`scroll-reveal ${visible ? 'visible' : ''}`}>
          <div className="text-center mb-16">
            <div className="inline-flex items-center gap-2 px-4 py-1.5 bg-indigo-50 rounded-full mb-6">
              <span className="w-2 h-2 bg-indigo-500 rounded-full" />
              <span className="text-indigo-600 text-sm font-medium">技术栈</span>
            </div>
            <h2 className="text-4xl md:text-5xl font-black text-gray-900 mb-4">
              技术<span className="text-gradient">架构</span>
            </h2>
            <p className="text-gray-500 text-lg">端侧AI + 云端服务，打造高性能低延迟的助残体验</p>
          </div>
        </div>

        {/* 技术卡片 */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-20">
          {techs.map((tech, i) => {
            const { ref: techRef, visible: techVisible } = useScrollReveal()
            return (
              <div key={i} ref={techRef} className={`scroll-reveal ${techVisible ? 'visible' : ''}`}
                style={{ transitionDelay: `${i * 0.1}s` }}>
                <div className={`card-hover bg-white rounded-2xl border-l-4 ${tech.color} p-6 shadow-sm`}>
                  <div className="text-2xl mb-4">{tech.icon}</div>
                  <h3 className="font-bold text-gray-900 mb-4">{tech.category}</h3>
                  <ul className="space-y-3">
                    {tech.items.map((item, j) => (
                      <li key={j} className="flex items-start gap-2.5">
                        <span className="w-1.5 h-1.5 bg-orange-500 rounded-full mt-1.5 flex-shrink-0" />
                        <div>
                          <p className="text-sm font-medium text-gray-700">{item.name}</p>
                          <p className="text-xs text-gray-400">{item.desc}</p>
                        </div>
                      </li>
                    ))}
                  </ul>
                </div>
              </div>
            )
          })}
        </div>

        {/* 数据流管道 */}
        <div className="bg-white rounded-3xl p-10 shadow-sm border border-gray-100">
          <h3 className="text-center font-bold text-gray-900 mb-8">识别数据流管道</h3>
          <div className="flex flex-col md:flex-row items-center justify-center gap-3 md:gap-0">
            {[
              { icon: '📷', label: '摄像头采集', sub: 'CameraX 640×640' },
              { icon: '🧠', label: 'MediaPipe', sub: '21点关键点提取' },
              { icon: '🔍', label: '向量DB匹配', sub: '余弦相似度' },
              { icon: '📊', label: '序列检测', sub: '动态语义识别' },
              { icon: '🗣️', label: 'TTS播报', sub: '语音合成输出' },
            ].map((step, i, arr) => (
              <div key={i} className="flex items-center">
                <div className="flex flex-col items-center gap-2 px-4 py-3 bg-gray-50 rounded-2xl min-w-[120px]">
                  <span className="text-2xl">{step.icon}</span>
                  <span className="text-xs font-semibold text-gray-700">{step.label}</span>
                  <span className="text-[10px] text-gray-400">{step.sub}</span>
                </div>
                {i < arr.length - 1 && (
                  <div className="flex items-center text-orange-400 text-xl mx-1 md:mx-2">
                    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
                    </svg>
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 安全对比区块
// ═══════════════════════════════════════════════════════════════════════════════
function SecurityComparison() {
  const { ref, visible } = useScrollReveal()

  const algorithms = [
    {
      name: 'AES-256',
      type: '对称加密',
      level: '军事级',
      speed: '极快',
      useCase: '数据存储加密',
      standard: 'FIPS 197',
      icon: '🔐',
    },
    {
      name: 'RSA-2048',
      type: '非对称加密',
      level: '高安全',
      speed: '较慢',
      useCase: '密钥交换/数字签名',
      standard: 'PKCS#1',
      icon: '🔑',
    },
    {
      name: 'TLS 1.3',
      type: '传输层协议',
      level: '行业标准',
      speed: '快',
      useCase: '网络通信加密',
      standard: 'RFC 8446',
      icon: '🌐',
    },
    {
      name: 'SHA-256',
      type: '哈希算法',
      level: '抗碰撞',
      speed: '快',
      useCase: '数据完整性校验',
      standard: 'FIPS 180-4',
      icon: '🔏',
    },
    {
      name: '端到端加密',
      type: '通信加密',
      level: '零知识',
      speed: '实时',
      useCase: '用户数据传输',
      standard: '端侧安全',
      icon: '🛡️',
      highlight: true,
    },
  ]

  return (
    <section className="py-28 bg-gradient-to-b from-gray-50 to-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div ref={ref} className={`scroll-reveal ${visible ? 'visible' : ''}`}>
          <div className="text-center mb-16">
            <div className="inline-flex items-center gap-2 px-4 py-1.5 bg-indigo-50 rounded-full mb-6">
              <span className="w-2 h-2 bg-indigo-500 rounded-full" />
              <span className="text-indigo-600 text-sm font-medium">安全对比</span>
            </div>
            <h2 className="text-4xl md:text-5xl font-black text-gray-900 mb-4">
              加密方案<span className="text-gradient">对比</span>
            </h2>
            <p className="text-gray-500 text-lg max-w-2xl mx-auto">
              主流加密算法与我们的端到端安全方案对比
            </p>
          </div>
        </div>

        {/* 对比表格 */}
        <div className="overflow-x-auto rounded-2xl border border-gray-100 shadow-sm">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-gray-50">
                <th className="text-left px-6 py-4 font-semibold text-gray-700">加密方案</th>
                <th className="text-left px-6 py-4 font-semibold text-gray-700">类型</th>
                <th className="text-left px-6 py-4 font-semibold text-gray-700">安全等级</th>
                <th className="text-left px-6 py-4 font-semibold text-gray-700">性能</th>
                <th className="text-left px-6 py-4 font-semibold text-gray-700">适用场景</th>
                <th className="text-left px-6 py-4 font-semibold text-gray-700">认证标准</th>
              </tr>
            </thead>
            <tbody>
              {algorithms.map((algo, i) => (
                <tr key={i} className={`border-t border-gray-50 transition-colors ${
                  algo.highlight
                    ? 'bg-gradient-to-r from-orange-50 to-indigo-50'
                    : 'hover:bg-gray-50'
                }`}>
                  <td className="px-6 py-5">
                    <div className="flex items-center gap-3">
                      <span className="text-xl">{algo.icon}</span>
                      <span className={`font-bold ${algo.highlight ? 'text-orange-600' : 'text-gray-900'}`}>
                        {algo.name}
                      </span>
                      {algo.highlight && (
                        <span className="px-2 py-0.5 bg-orange-500 text-white text-[10px] font-bold rounded-full">
                          我们
                        </span>
                      )}
                    </div>
                  </td>
                  <td className="px-6 py-5 text-gray-600">{algo.type}</td>
                  <td className="px-6 py-5">
                    <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium ${
                      algo.highlight
                        ? 'bg-orange-100 text-orange-700'
                        : 'bg-gray-100 text-gray-600'
                    }`}>
                      <span className={`w-1.5 h-1.5 rounded-full ${
                        algo.highlight ? 'bg-orange-500' : 'bg-gray-400'
                      }`} />
                      {algo.level}
                    </span>
                  </td>
                  <td className="px-6 py-5">
                    <span className="text-gray-600">{algo.speed}</span>
                  </td>
                  <td className="px-6 py-5 text-gray-600">{algo.useCase}</td>
                  <td className="px-6 py-5">
                    <span className="text-xs text-gray-400 font-mono">{algo.standard}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* 底部说明 */}
        <div className="mt-8 grid grid-cols-1 md:grid-cols-3 gap-6">
          {[
            { icon: '🔒', title: '传输加密', desc: '端到端加密通道，防止中间人攻击，保障用户数据在传输过程中不被窃取或篡改' },
            { icon: '📱', title: '端侧处理', desc: '所有AI推理在设备本地完成，敏感数据从不出设备，从根本上杜绝云端泄露风险' },
            { icon: '✅', title: '合规认证', desc: '遵循《个人信息保护法》要求，最小化数据采集原则，用户可随时删除所有本地数据' },
          ].map((item, i) => (
            <div key={i} className="bg-white rounded-2xl p-6 border border-gray-100">
              <div className="text-2xl mb-3">{item.icon}</div>
              <h4 className="font-bold text-gray-900 mb-2">{item.title}</h4>
              <p className="text-gray-500 text-sm leading-relaxed">{item.desc}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 团队区块
// ═══════════════════════════════════════════════════════════════════════════════
function TeamSection() {
  const { ref, visible } = useScrollReveal()
  const members = [
    { role: '项目负责人', skills: '产品设计 · 项目管理', emoji: '🎯', color: 'from-orange-500 to-red-500' },
    { role: 'Android 开发', skills: 'Kotlin · Jetpack Compose', emoji: '📱', color: 'from-blue-500 to-cyan-500' },
    { role: 'AI 算法工程师', skills: 'MediaPipe · TFLite · Python', emoji: '🧠', color: 'from-purple-500 to-pink-500' },
    { role: 'UI/UX 设计师', skills: 'Figma · 交互设计', emoji: '🎨', color: 'from-green-500 to-emerald-500' },
  ]

  return (
    <section id="team" className="py-28 bg-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div ref={ref} className={`scroll-reveal ${visible ? 'visible' : ''}`}>
          <div className="text-center mb-16">
            <div className="inline-flex items-center gap-2 px-4 py-1.5 bg-amber-50 rounded-full mb-6">
              <span className="w-2 h-2 bg-amber-500 rounded-full" />
              <span className="text-amber-600 text-sm font-medium">团队成员</span>
            </div>
            <h2 className="text-4xl md:text-5xl font-black text-gray-900 mb-4">
              我们的<span className="text-gradient">团队</span>
            </h2>
            <p className="text-gray-500 text-lg">心怀微光，同行为善</p>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {members.map((member, i) => {
            const { ref: mRef, visible: mVisible } = useScrollReveal()
            return (
              <div key={i} ref={mRef} className={`scroll-reveal ${mVisible ? 'visible' : ''}`}
                style={{ transitionDelay: `${i * 0.1}s` }}>
                <div className="card-hover text-center p-8 bg-gray-50 rounded-3xl border border-gray-100">
                  <div className={`w-20 h-20 bg-gradient-to-br ${member.color} rounded-2xl mx-auto mb-5 flex items-center justify-center text-3xl shadow-lg`}>
                    {member.emoji}
                  </div>
                  <h3 className="font-bold text-gray-900 mb-2">{member.role}</h3>
                  <p className="text-gray-500 text-sm">{member.skills}</p>
                </div>
              </div>
            )
          })}
        </div>
      </div>
    </section>
  )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 页脚
// ═══════════════════════════════════════════════════════════════════════════════
function FooterSection() {
  return (
    <footer id="about" className="relative bg-gray-950 text-white pt-24 pb-12 overflow-hidden">
      {/* 背景装饰 */}
      <div className="absolute top-0 left-0 right-0 h-px bg-gradient-to-r from-transparent via-orange-500/50 to-transparent" />
      <div className="absolute top-20 right-20 w-64 h-64 bg-orange-500/10 rounded-full blur-[100px]" />

      <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-12 mb-12">
          <div>
            <div className="flex items-center gap-2.5 mb-5">
              <div className="w-9 h-9 bg-gradient-to-br from-orange-500 to-red-500 rounded-xl flex items-center justify-center text-lg">🌟</div>
              <span className="font-bold text-xl">微光<span className="text-orange-500">同行</span></span>
            </div>
            <p className="text-gray-400 text-sm leading-relaxed mb-6">
              智能助残服务平台，致力于通过AI技术改善听障人士的生活质量。
              项目服务于大学生创新创业大赛红旅赛道、成都助残比赛及科大讯飞AI开发者大赛。
            </p>
            <div className="flex gap-3">
              {['🏆', '🤝', '💡'].map((emoji, i) => (
                <div key={i} className="w-10 h-10 bg-white/5 rounded-xl flex items-center justify-center text-lg hover:bg-white/10 transition-colors cursor-pointer">
                  {emoji}
                </div>
              ))}
            </div>
          </div>

          <div>
            <h3 className="font-bold text-lg mb-5">快速导航</h3>
            <ul className="space-y-3">
              {[
                { label: '核心功能', href: '#features' },
                { label: 'SOS 紧急求助', href: '#sos' },
                { label: '技术架构', href: '#tech' },
                { label: '团队成员', href: '#team' },
              ].map((item, i) => (
                <li key={i}>
                  <a href={item.href} className="text-gray-400 hover:text-orange-400 text-sm transition-colors flex items-center gap-2 group">
                    <span className="w-0 h-0.5 bg-orange-500 group-hover:w-3 transition-all" />
                    {item.label}
                  </a>
                </li>
              ))}
            </ul>
          </div>

          <div>
            <h3 className="font-bold text-lg mb-5">联系我们</h3>
            <div className="space-y-4 text-sm text-gray-400">
              <div className="flex items-center gap-3">
                <span className="text-lg">📧</span>
                <span>weiguangtongxing@example.com</span>
              </div>
              <div className="flex items-center gap-3">
                <span className="text-lg">📍</span>
                <span>四川省成都市</span>
              </div>
              <div className="flex items-center gap-3">
                <span className="text-lg">🏆</span>
                <span>大学生创新创业大赛 · 红旅赛道</span>
              </div>
            </div>
          </div>
        </div>

        <div className="pt-8 border-t border-gray-800 flex flex-col md:flex-row justify-between items-center gap-4">
          <p className="text-gray-500 text-sm">© 2026 微光同行 · 智能助残服务平台</p>
          <p className="text-gray-600 text-xs">所有AI算法在设备端本地运行，保护用户隐私安全</p>
        </div>
      </div>
    </footer>
  )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 返回顶部按钮
// ═══════════════════════════════════════════════════════════════════════════════
function BackToTop() {
  const [visible, setVisible] = useState(false)

  useEffect(() => {
    const onScroll = () => setVisible(window.scrollY > 500)
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [])

  if (!visible) return null

  return (
    <button
      onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
      className="fixed bottom-8 right-8 z-40 w-12 h-12 bg-gradient-to-br from-orange-500 to-red-500 text-white rounded-2xl shadow-lg shadow-orange-500/25 hover:shadow-xl hover:scale-110 transition-all duration-300 flex items-center justify-center"
    >
      <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M5 15l7-7 7 7" />
      </svg>
    </button>
  )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 主组件
// ═══════════════════════════════════════════════════════════════════════════════
export default function App() {
  return (
    <div className="min-h-screen">
      <Navbar />
      <HeroSection />
      <FeaturesSection />
      <StatsSection />
      <SosSection />
      <TechSection />
      <SecurityComparison />
      <TeamSection />
      <FooterSection />
      <BackToTop />
    </div>
  )
}
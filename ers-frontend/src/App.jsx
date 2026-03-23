import React, { useEffect, useMemo, useState } from 'react'
import {
  BarChart3, ClipboardList, Laptop, LayoutDashboard, Link2, Lock, LogOut, Plus, RefreshCcw, Search, ShieldCheck, UserCheck, UserX, Users,
} from 'lucide-react'
import { Bar, BarChart, CartesianGrid, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'

const API_BASE = '/api'

async function apiFetch(path, options) {
  const response = await fetch(`${API_BASE}${path}`, options)
  const text = await response.text()
  let data = null
  try { data = text ? JSON.parse(text) : null } catch { data = text }
  if (!response.ok) throw new Error(data?.message || text || 'Erro na requisição')
  return data
}

function formatMoney(value) {
  return `R$ ${Number(value || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

function Panel({ title, subtitle, actions, children }) {
  return <section className="rounded-[28px] border border-white/10 bg-white/5 p-5 shadow-xl backdrop-blur"><div className="mb-4 flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between"><div><h2 className="text-xl font-semibold text-white">{title}</h2>{subtitle && <p className="text-sm text-slate-400">{subtitle}</p>}</div>{actions}</div>{children}</section>
}
function Button({ children, className = '', ...props }) {
  return <button {...props} className={`inline-flex items-center justify-center rounded-2xl px-4 py-3 text-sm font-medium transition disabled:cursor-not-allowed disabled:opacity-50 ${className}`}>{children}</button>
}
function Input(props) {
  return <input {...props} className={`w-full rounded-2xl border border-white/10 bg-slate-950/70 px-4 py-3 text-sm text-white outline-none placeholder:text-slate-500 focus:border-emerald-400 ${props.className || ''}`} />
}
function Select(props) {
  return <select {...props} className={`w-full rounded-2xl border border-white/10 bg-slate-950/70 px-4 py-3 text-sm text-white outline-none focus:border-emerald-400 ${props.className || ''}`} />
}
function StatCard({ title, value, hint, icon: Icon }) {
  return <div className="rounded-[26px] border border-white/10 bg-white/5 p-5 shadow-xl"><div className="flex items-start justify-between gap-4"><div><p className="text-sm text-slate-400">{title}</p><h3 className="mt-2 text-3xl font-semibold text-white">{value}</h3><p className="mt-2 text-sm text-emerald-300">{hint}</p></div><div className="rounded-2xl bg-emerald-500/15 p-3 text-emerald-300"><Icon className="h-5 w-5" /></div></div></div>
}
function EmptyRow({ colSpan, text }) { return <tr><td className="px-4 py-8 text-center text-slate-400" colSpan={colSpan}>{text}</td></tr> }


function ConfirmModal({ open, title, description, onCancel, onConfirm, loading }) {
  if (!open) return null
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4">
      <div className="w-full max-w-md rounded-[28px] border border-white/10 bg-slate-900 p-6 shadow-2xl">
        <h3 className="text-xl font-semibold text-white">{title}</h3>
        <p className="mt-2 text-sm text-slate-400">{description}</p>
        <div className="mt-6 flex justify-end gap-3">
          <Button onClick={onCancel} disabled={loading} className="border border-white/10 bg-slate-800 text-white hover:bg-slate-700">Cancelar</Button>
          <Button onClick={onConfirm} disabled={loading} className="bg-rose-600 text-white hover:bg-rose-700">{loading ? 'Removendo...' : 'Confirmar remoção'}</Button>
        </div>
      </div>
    </div>
  )
}

function LoginScreen({ onLogin, loginForm, setLoginForm, loginLoading, loginError }) {
  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-950 px-4">
      <div className="w-full max-w-md rounded-[32px] border border-white/10 bg-white/5 p-8 shadow-2xl backdrop-blur">
        <div className="mb-6 text-center">
          <div className="mx-auto inline-flex rounded-full bg-emerald-500/15 px-4 py-1 text-sm font-medium text-emerald-300">ERS Platform</div>
          <h1 className="mt-4 text-3xl font-semibold text-white">Entrar no painel</h1>
          <p className="mt-2 text-sm text-slate-400">Use admin/123 ou user/123 para acessar com permissões diferentes.</p>
        </div>
        <form className="space-y-4" onSubmit={onLogin}>
          <div>
            <label className="mb-2 block text-sm text-slate-300">Usuário</label>
            <Input value={loginForm.username} onChange={(e)=>setLoginForm({...loginForm, username:e.target.value})} placeholder="admin" required />
          </div>
          <div>
            <label className="mb-2 block text-sm text-slate-300">Senha</label>
            <Input type="password" value={loginForm.password} onChange={(e)=>setLoginForm({...loginForm, password:e.target.value})} placeholder="123" required />
          </div>
          {loginError && <div className="rounded-2xl border border-rose-500/20 bg-rose-500/10 px-4 py-3 text-sm text-rose-300">{loginError}</div>}
          <Button type="submit" disabled={loginLoading} className="w-full bg-emerald-600 text-white hover:bg-emerald-700">
            <Lock className="mr-2 h-4 w-4" /> {loginLoading ? 'Entrando...' : 'Entrar'}
          </Button>
        </form>
      </div>
    </div>
  )
}

export default function App() {
  const [page, setPage] = useState('dashboard')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [toast, setToast] = useState('')
  const [pendingDelete, setPendingDelete] = useState(null)
  const [deleting, setDeleting] = useState(false)
  const [data, setData] = useState({ colaboradores: [], recursos: [], alocacoes: [] })
  const [search, setSearch] = useState('')
  const [loginForm, setLoginForm] = useState({ username: '', password: '' })
  const [loginLoading, setLoginLoading] = useState(false)
  const [loginError, setLoginError] = useState('')
  const [user, setUser] = useState(() => { try { return JSON.parse(localStorage.getItem('ers_user') || 'null') } catch { return null } })

  const [novoColaborador, setNovoColaborador] = useState({ nome: '', cargo: '', salario: '', data: '' })
  const [novoRecurso, setNovoRecurso] = useState({ nome: '', categoria: 'Notebook', valor: '' })
  const [novaAlocacao, setNovaAlocacao] = useState({ colaboradorId: '', recursoId: '', observacao: 'Alocação via frontend', autorizado: false })

  const isAdmin = user?.role === 'ADMIN'

  async function loadAll() {
    try {
      setLoading(true); setError('')
      const [colaboradores, recursos, alocacoes] = await Promise.all([apiFetch('/colaboradores'), apiFetch('/recursos'), apiFetch('/alocacoes')])
      setData({ colaboradores, recursos, alocacoes })
    } catch (err) { setError(err.message || 'Não foi possível carregar a API') } finally { setLoading(false) }
  }

  useEffect(() => { if (user) loadAll() }, [user])
  useEffect(() => { if (!toast) return; const t = setTimeout(()=>setToast(''),2800); return ()=>clearTimeout(t) }, [toast])

  async function fazerLogin(e) {
    e.preventDefault()
    try {
      setLoginLoading(true); setLoginError('')
      const result = await apiFetch('/login', { method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded;charset=UTF-8'}, body:new URLSearchParams(loginForm) })
      setUser(result)
      localStorage.setItem('ers_user', JSON.stringify(result))
      setToast(`Login realizado como ${result.role}.`)
    } catch (err) {
      setLoginError(err.message || 'Falha no login')
    } finally { setLoginLoading(false) }
  }
  function logout() { setUser(null); localStorage.removeItem('ers_user'); setPage('dashboard'); setData({ colaboradores: [], recursos: [], alocacoes: [] }) }

  const ativos = data.colaboradores.filter((c) => c.ativo).length
  const inativos = data.colaboradores.length - ativos
  const disponiveis = data.recursos.filter((r) => r.disponivel).length
  const alocacoesAtivas = data.alocacoes.filter((a) => a.ativa).length
  const custoTotal = data.recursos.reduce((acc, item) => acc + Number(item.valor || 0), 0)
  const colaboradoresFiltrados = useMemo(() => { const term = search.toLowerCase().trim(); if (!term) return data.colaboradores; return data.colaboradores.filter((c) => [String(c.id), c.nome, c.cargo].some((v) => String(v || '').toLowerCase().includes(term))) }, [data.colaboradores, search])
  const recursosFiltrados = useMemo(() => { const term = search.toLowerCase().trim(); if (!term) return data.recursos; return data.recursos.filter((r) => [String(r.id), r.nome, r.categoria].some((v) => String(v || '').toLowerCase().includes(term))) }, [data.recursos, search])
  const colaboradoresById = useMemo(() => Object.fromEntries(data.colaboradores.map((c) => [c.id, c])), [data.colaboradores])
  const recursosById = useMemo(() => Object.fromEntries(data.recursos.map((r) => [r.id, r])), [data.recursos])
  const chartStatus = [{ name: 'Ativos', value: ativos }, { name: 'Inativos', value: inativos }]
  const chartResumo = [{ nome: 'Colaboradores', total: data.colaboradores.length }, { nome: 'Recursos', total: data.recursos.length }, { nome: 'Alocações', total: data.alocacoes.length }]
  const chartRecursos = useMemo(() => { const groups = {}; for (const r of data.recursos) groups[r.categoria] = (groups[r.categoria] || 0) + 1; return Object.entries(groups).map(([nome,total])=>({nome,total})) }, [data.recursos])

  async function salvarColaborador(e) { e.preventDefault(); try { await apiFetch('/colaboradores',{ method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded;charset=UTF-8'}, body:new URLSearchParams(novoColaborador) }); setNovoColaborador({ nome: '', cargo: '', salario: '', data: '' }); setToast('Colaborador cadastrado com sucesso.'); await loadAll(); setPage('colaboradores') } catch (err) { setError(err.message) } }
  async function salvarRecurso(e) { e.preventDefault(); try { await apiFetch('/recursos',{ method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded;charset=UTF-8'}, body:new URLSearchParams(novoRecurso) }); setNovoRecurso({ nome: '', categoria: 'Notebook', valor: '' }); setToast('Recurso cadastrado com sucesso.'); await loadAll(); setPage('recursos') } catch (err) { setError(err.message) } }
  async function salvarAlocacao(e) { e.preventDefault(); try { await apiFetch('/alocar',{ method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded;charset=UTF-8'}, body:new URLSearchParams({ ...novaAlocacao, autorizado: String(!!novaAlocacao.autorizado) }) }); setNovaAlocacao({ colaboradorId: '', recursoId: '', observacao: 'Alocação via frontend', autorizado: false }); setToast('Alocação registrada com sucesso.'); await loadAll(); setPage('alocacoes') } catch (err) { setError(err.message) } }
  async function alterarStatus(id, ativo) { try { await apiFetch('/colaboradores/status',{ method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded;charset=UTF-8'}, body:new URLSearchParams({ id:String(id), ativo:String(ativo) }) }); setToast(`Colaborador ${ativo ? 'ativado' : 'desativado'} com sucesso.`); await loadAll() } catch (err) { setError(err.message) } }
  async function devolverAlocacao(recursoId) { try { await apiFetch('/devolver',{ method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded;charset=UTF-8'}, body:new URLSearchParams({ recursoId:String(recursoId), observacao:'Devolução via frontend' }) }); setToast('Recurso devolvido com sucesso.'); await loadAll() } catch (err) { setError(err.message) } }
  async function removerColaborador(id) { try { setDeleting(true); await apiFetch('/remover-colaborador',{ method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded;charset=UTF-8'}, body:new URLSearchParams({ id:String(id) }) }); setToast('Colaborador removido com sucesso.'); await loadAll() } catch (err) { setError(err.message) } finally { setDeleting(false); setPendingDelete(null) } }
  async function removerRecurso(id) { try { setDeleting(true); await apiFetch('/remover-recurso',{ method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded;charset=UTF-8'}, body:new URLSearchParams({ id:String(id) }) }); setToast('Recurso removido com sucesso.'); await loadAll() } catch (err) { setError(err.message) } finally { setDeleting(false); setPendingDelete(null) } }

  const navItems = [
    { key: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { key: 'colaboradores', label: 'Colaboradores', icon: Users },
    { key: 'recursos', label: 'Recursos', icon: Laptop },
    { key: 'alocacoes', label: 'Alocações', icon: Link2 },
    { key: 'relatorios', label: 'Relatórios', icon: BarChart3 },
    ...(isAdmin ? [{ key: 'cadastros', label: 'Cadastros', icon: Plus }] : []),
  ]

  if (!user) return <LoginScreen onLogin={fazerLogin} loginForm={loginForm} setLoginForm={setLoginForm} loginLoading={loginLoading} loginError={loginError} />

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100">
      <div className="mx-auto grid max-w-7xl gap-6 px-4 py-6 lg:grid-cols-[260px_1fr] lg:px-6">
        <aside className="rounded-[30px] border border-white/10 bg-white/5 p-5 shadow-2xl backdrop-blur">
          <div className="mb-8">
            <div className="inline-flex rounded-full bg-emerald-500/15 px-3 py-1 text-sm font-medium text-emerald-300">ERS Platform</div>
            <h1 className="mt-4 text-2xl font-semibold text-white">Employee Resource System</h1>
            <p className="mt-2 text-sm leading-6 text-slate-400">Painel React com controle por perfil. ADMIN gerencia tudo; USER consulta e acompanha o sistema.</p>
          </div>
          <nav className="space-y-2">
            {navItems.map(({ key, label, icon: Icon }) => { const active = page === key; return <button key={key} onClick={() => { setSearch(''); setPage(key) }} className={`flex w-full items-center gap-3 rounded-2xl px-4 py-3 text-left text-sm font-medium transition ${active ? 'bg-emerald-600 text-white shadow-lg shadow-emerald-500/20' : 'text-slate-300 hover:bg-white/5'}`}><Icon className="h-4 w-4" />{label}</button> })}
          </nav>
          <div className="mt-8 space-y-3 rounded-[24px] border border-white/10 bg-slate-950/50 p-4">
            <div className="rounded-2xl bg-white/5 p-3">
              <div className="flex items-center gap-2 text-sm text-white"><ShieldCheck className="h-4 w-4 text-emerald-300" /> {user.username}</div>
              <p className="mt-1 text-xs text-slate-400">Perfil: {user.role === 'ADMIN' ? 'Administrador' : 'Usuário'}</p>
            </div>
            <Button onClick={loadAll} className="w-full bg-white text-slate-900 hover:bg-slate-100"><RefreshCcw className={`mr-2 h-4 w-4 ${loading ? 'animate-spin' : ''}`} />{loading ? 'Atualizando...' : 'Atualizar dados'}</Button>
            <Button onClick={logout} className="w-full border border-white/10 bg-slate-950/60 text-white hover:bg-slate-900"><LogOut className="mr-2 h-4 w-4" />Sair</Button>
          </div>
        </aside>

        <main className="space-y-6">
          <header className="rounded-[32px] bg-gradient-to-br from-emerald-500 to-cyan-500 p-6 text-white shadow-2xl shadow-emerald-500/20">
            <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
              <div>
                <p className="text-sm text-white/80">ERS Frontend • {isAdmin ? 'Modo ADMIN' : 'Modo USER'}</p>
                <h2 className="mt-2 text-3xl font-semibold">{page === 'dashboard' && 'Painel geral do sistema'}{page === 'colaboradores' && 'Gestão de colaboradores'}{page === 'recursos' && 'Inventário de recursos'}{page === 'alocacoes' && 'Controle de alocações'}{page === 'relatorios' && 'Indicadores e relatórios'}{page === 'cadastros' && 'Cadastros rápidos'}</h2>
                <p className="mt-3 max-w-2xl text-sm text-white/90">{isAdmin ? 'Você pode cadastrar, ativar e desativar funcionários, além de gerenciar inventário e alocações.' : 'Você pode consultar dashboards, colaboradores, recursos, alocações e relatórios.'}</p>
              </div>
            </div>
          </header>

          {error && <div className="rounded-2xl border border-rose-500/20 bg-rose-500/10 px-4 py-3 text-sm text-rose-300">{error}</div>}
          {toast && <div className="rounded-2xl border border-emerald-500/20 bg-emerald-500/10 px-4 py-3 text-sm text-emerald-300">{toast}</div>}

          {page === 'dashboard' && <>
            <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
              <StatCard title="Colaboradores" value={data.colaboradores.length} hint={`${ativos} ativos`} icon={Users} />
              <StatCard title="Recursos" value={data.recursos.length} hint={`${disponiveis} disponíveis`} icon={Laptop} />
              <StatCard title="Alocações" value={data.alocacoes.length} hint={`${alocacoesAtivas} ativas`} icon={ClipboardList} />
              <StatCard title="Inventário" value={formatMoney(custoTotal)} hint="Valor total estimado" icon={BarChart3} />
            </section>
            <section className="grid gap-6 xl:grid-cols-[1.15fr_0.85fr]">
              <Panel title="Resumo do sistema" subtitle="Visão rápida das entidades principais e sua distribuição."><div className="h-72"><ResponsiveContainer width="100%" height="100%"><BarChart data={chartResumo}><CartesianGrid strokeDasharray="3 3" stroke="#334155" /><XAxis dataKey="nome" stroke="#94a3b8" /><YAxis stroke="#94a3b8" /><Tooltip /><Bar dataKey="total" fill="#10b981" radius={[8,8,0,0]} /></BarChart></ResponsiveContainer></div></Panel>
              <Panel title="Status dos colaboradores" subtitle="Ativos e inativos no momento."><div className="h-72"><ResponsiveContainer width="100%" height="100%"><PieChart><Pie data={chartStatus} dataKey="value" nameKey="name" outerRadius={88} label><Cell fill="#10b981" /><Cell fill="#f59e0b" /></Pie><Tooltip /></PieChart></ResponsiveContainer></div></Panel>
            </section></>}

          {page === 'colaboradores' && <Panel title="Colaboradores" subtitle={isAdmin ? 'Busque, ative e desative funcionários.' : 'Visualização somente leitura para usuários comuns.'} actions={<div className="relative w-full max-w-sm"><Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" /><Input value={search} onChange={(e)=>setSearch(e.target.value)} placeholder="Buscar por nome, cargo ou ID" className="pl-10" /></div>}>
            <div className="overflow-hidden rounded-2xl border border-white/10"><div className="overflow-x-auto"><table className="min-w-full text-left text-sm"><thead className="bg-slate-900/80 text-slate-400"><tr><th className="px-4 py-3">ID</th><th className="px-4 py-3">Nome</th><th className="px-4 py-3">Cargo</th><th className="px-4 py-3">Salário</th><th className="px-4 py-3">Status</th>{isAdmin && <th className="px-4 py-3">Ações</th>}</tr></thead><tbody>{colaboradoresFiltrados.map((c)=><tr key={c.id} className="border-t border-white/10 bg-slate-950/30"><td className="px-4 py-3">{c.id}</td><td className="px-4 py-3">{c.nome}</td><td className="px-4 py-3">{c.cargo}</td><td className="px-4 py-3">{formatMoney(c.salario)}</td><td className="px-4 py-3"><span className={`rounded-full px-3 py-1 text-xs font-medium ${c.ativo ? 'bg-emerald-500/15 text-emerald-300' : 'bg-amber-500/15 text-amber-300'}`}>{c.ativo ? 'Ativo' : 'Inativo'}</span></td>{isAdmin && <td className="px-4 py-3"><div className="flex flex-wrap gap-2"><Button onClick={()=>alterarStatus(c.id,true)} disabled={c.ativo} className="bg-emerald-600 text-white hover:bg-emerald-700"><UserCheck className="mr-2 h-4 w-4" />Ativar</Button><Button onClick={()=>alterarStatus(c.id,false)} disabled={!c.ativo} className="bg-rose-600 text-white hover:bg-rose-700"><UserX className="mr-2 h-4 w-4" />Desativar</Button><Button onClick={()=>setPendingDelete({ type:'colaborador', id:c.id, name:c.nome })} className="bg-slate-700 text-white hover:bg-slate-600">Remover</Button></div></td>}</tr>)}{!colaboradoresFiltrados.length && <EmptyRow colSpan={isAdmin ? 6 : 5} text="Nenhum colaborador encontrado." />}</tbody></table></div></div>
          </Panel>}

          {page === 'recursos' && <div className="grid gap-6 xl:grid-cols-[0.9fr_1.1fr]">
            {isAdmin && <Panel title="Cadastrar recurso" subtitle="Adicione ativos ao inventário da empresa."><form className="space-y-4" onSubmit={salvarRecurso}><Input value={novoRecurso.nome} onChange={(e)=>setNovoRecurso({...novoRecurso, nome:e.target.value})} placeholder="Nome do recurso" required /><Select value={novoRecurso.categoria} onChange={(e)=>setNovoRecurso({...novoRecurso, categoria:e.target.value})}><option>Notebook</option><option>Celular</option><option>Monitor</option><option>Licença</option><option>Periférico</option></Select><Input type="number" min="0" step="0.01" value={novoRecurso.valor} onChange={(e)=>setNovoRecurso({...novoRecurso, valor:e.target.value})} placeholder="Valor estimado" required /><Button type="submit" className="w-full bg-cyan-600 text-white hover:bg-cyan-700"><Plus className="mr-2 h-4 w-4" />Salvar recurso</Button></form></Panel>}
            <Panel title="Inventário" subtitle="Visualização completa dos recursos cadastrados." actions={<div className="relative w-full max-w-sm"><Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" /><Input value={search} onChange={(e)=>setSearch(e.target.value)} placeholder="Buscar recurso" className="pl-10" /></div>}><div className="overflow-hidden rounded-2xl border border-white/10"><div className="overflow-x-auto"><table className="min-w-full text-left text-sm"><thead className="bg-slate-900/80 text-slate-400"><tr><th className="px-4 py-3">ID</th><th className="px-4 py-3">Nome</th><th className="px-4 py-3">Categoria</th><th className="px-4 py-3">Status</th><th className="px-4 py-3">Valor</th>{isAdmin && <th className="px-4 py-3">Ações</th>}</tr></thead><tbody>{recursosFiltrados.map((r)=><tr key={r.id} className="border-t border-white/10 bg-slate-950/30"><td className="px-4 py-3">{r.id}</td><td className="px-4 py-3">{r.nome}</td><td className="px-4 py-3">{r.categoria}</td><td className="px-4 py-3"><span className={`rounded-full px-3 py-1 text-xs font-medium ${r.disponivel ? 'bg-emerald-500/15 text-emerald-300' : 'bg-cyan-500/15 text-cyan-300'}`}>{r.disponivel ? 'Disponível' : 'Alocado'}</span></td><td className="px-4 py-3">{formatMoney(r.valor)}</td>{isAdmin && <td className="px-4 py-3"><Button onClick={()=>setPendingDelete({ type:'recurso', id:r.id, name:r.nome })} className="bg-slate-700 text-white hover:bg-slate-600">Remover</Button></td>}</tr>)}{!recursosFiltrados.length && <EmptyRow colSpan={isAdmin ? 6 : 5} text="Nenhum recurso encontrado." />}</tbody></table></div></div></Panel>
          </div>}

          {page === 'alocacoes' && <div className="grid gap-6 xl:grid-cols-[0.9fr_1.1fr]">
            {isAdmin && <Panel title="Nova alocação" subtitle="Associe um recurso disponível a um colaborador ativo. Recursos acima de R$ 5.000 exigem autorização especial."><form className="space-y-4" onSubmit={salvarAlocacao}><Select value={novaAlocacao.colaboradorId} onChange={(e)=>setNovaAlocacao({...novaAlocacao, colaboradorId:e.target.value})} required><option value="">Selecione o colaborador</option>{data.colaboradores.map((c)=><option key={c.id} value={c.id}>{`${c.id} - ${c.nome}`}</option>)}</Select><Select value={novaAlocacao.recursoId} onChange={(e)=>setNovaAlocacao({...novaAlocacao, recursoId:e.target.value})} required><option value="">Selecione o recurso</option>{data.recursos.map((r)=><option key={r.id} value={r.id}>{`${r.id} - ${r.nome} • ${formatMoney(r.valor)}`}</option>)}</Select><Input value={novaAlocacao.observacao} onChange={(e)=>setNovaAlocacao({...novaAlocacao, observacao:e.target.value})} placeholder="Observação" /><label className="flex items-center gap-3 rounded-2xl border border-amber-500/20 bg-amber-500/10 px-4 py-3 text-sm text-amber-200"><input type="checkbox" checked={!!novaAlocacao.autorizado} onChange={(e)=>setNovaAlocacao({...novaAlocacao, autorizado:e.target.checked})} className="h-4 w-4 rounded border-white/10 bg-slate-900" />Autorizar alocação especial para recurso acima de R$ 5.000</label><Button type="submit" className="w-full bg-violet-600 text-white hover:bg-violet-700"><Link2 className="mr-2 h-4 w-4" />Registrar alocação</Button></form></Panel>}
            <Panel title="Histórico de alocações" subtitle="Acompanhe quais recursos estão ou estiveram vinculados a colaboradores."><div className="overflow-hidden rounded-2xl border border-white/10"><div className="overflow-x-auto"><table className="min-w-full text-left text-sm"><thead className="bg-slate-900/80 text-slate-400"><tr><th className="px-4 py-3">ID</th><th className="px-4 py-3">Colaborador</th><th className="px-4 py-3">Recurso</th><th className="px-4 py-3">Status</th>{isAdmin && <th className="px-4 py-3">Ações</th>}</tr></thead><tbody>{data.alocacoes.map((a)=><tr key={a.id} className="border-t border-white/10 bg-slate-950/30"><td className="px-4 py-3">{a.id}</td><td className="px-4 py-3">{colaboradoresById[a.colaboradorId]?.nome || `ID ${a.colaboradorId}`}</td><td className="px-4 py-3">{recursosById[a.recursoId]?.nome || `ID ${a.recursoId}`}</td><td className="px-4 py-3"><span className={`rounded-full px-3 py-1 text-xs font-medium ${a.ativa ? 'bg-emerald-500/15 text-emerald-300' : 'bg-slate-500/15 text-slate-300'}`}>{a.ativa ? 'Ativa' : 'Encerrada'}</span></td>{isAdmin && <td className="px-4 py-3">{a.ativa ? <Button onClick={()=>devolverAlocacao(a.recursoId)} className="bg-amber-600 text-white hover:bg-amber-700">Devolver</Button> : <span className="text-slate-500">—</span>}</td>}</tr>)}{!data.alocacoes.length && <EmptyRow colSpan={isAdmin ? 5 : 4} text="Nenhuma alocação encontrada." />}</tbody></table></div></div></Panel>
          </div>}

          {page === 'relatorios' && <div className="grid gap-6 xl:grid-cols-2"><Panel title="Recursos por categoria" subtitle="Distribuição atual do inventário cadastrado."><div className="h-80"><ResponsiveContainer width="100%" height="100%"><BarChart data={chartRecursos}><CartesianGrid strokeDasharray="3 3" stroke="#334155" /><XAxis dataKey="nome" stroke="#94a3b8" /><YAxis stroke="#94a3b8" /><Tooltip /><Bar dataKey="total" fill="#06b6d4" radius={[8,8,0,0]} /></BarChart></ResponsiveContainer></div></Panel><Panel title="Resumo executivo" subtitle="Leitura rápida para apresentação do projeto."><div className="grid gap-4 md:grid-cols-2"><div className="rounded-2xl bg-slate-950/50 p-4"><p className="text-sm text-slate-400">Ativos humanos</p><h3 className="mt-2 text-2xl font-semibold text-white">{ativos}</h3></div><div className="rounded-2xl bg-slate-950/50 p-4"><p className="text-sm text-slate-400">Funcionários inativos</p><h3 className="mt-2 text-2xl font-semibold text-white">{inativos}</h3></div><div className="rounded-2xl bg-slate-950/50 p-4"><p className="text-sm text-slate-400">Recursos disponíveis</p><h3 className="mt-2 text-2xl font-semibold text-white">{disponiveis}</h3></div><div className="rounded-2xl bg-slate-950/50 p-4"><p className="text-sm text-slate-400">Valor do inventário</p><h3 className="mt-2 text-2xl font-semibold text-white">{formatMoney(custoTotal)}</h3></div></div></Panel></div>}

          {isAdmin && page === 'cadastros' && <div className="grid gap-6 xl:grid-cols-2"><Panel title="Novo colaborador" subtitle="Cadastro rápido direto pela API."><form className="space-y-4" onSubmit={salvarColaborador}><Input value={novoColaborador.nome} onChange={(e)=>setNovoColaborador({...novoColaborador, nome:e.target.value})} placeholder="Nome do colaborador" required /><Input value={novoColaborador.cargo} onChange={(e)=>setNovoColaborador({...novoColaborador, cargo:e.target.value})} placeholder="Cargo" required /><Input type="number" min="0" step="0.01" value={novoColaborador.salario} onChange={(e)=>setNovoColaborador({...novoColaborador, salario:e.target.value})} placeholder="Salário" required /><Input type="date" value={novoColaborador.data} onChange={(e)=>setNovoColaborador({...novoColaborador, data:e.target.value})} required /><Button type="submit" className="w-full bg-emerald-600 text-white hover:bg-emerald-700"><Plus className="mr-2 h-4 w-4" />Salvar colaborador</Button></form></Panel><Panel title="Atalhos" subtitle="Ações rápidas para navegação no sistema."><div className="grid gap-3"><Button onClick={()=>setPage('colaboradores')} className="justify-start border border-white/10 bg-slate-950/50 text-white hover:bg-slate-900"><Users className="mr-2 h-4 w-4" />Ir para colaboradores</Button><Button onClick={()=>setPage('recursos')} className="justify-start border border-white/10 bg-slate-950/50 text-white hover:bg-slate-900"><Laptop className="mr-2 h-4 w-4" />Ir para recursos</Button><Button onClick={()=>setPage('alocacoes')} className="justify-start border border-white/10 bg-slate-950/50 text-white hover:bg-slate-900"><Link2 className="mr-2 h-4 w-4" />Ir para alocações</Button><Button onClick={()=>setPage('relatorios')} className="justify-start border border-white/10 bg-slate-950/50 text-white hover:bg-slate-900"><BarChart3 className="mr-2 h-4 w-4" />Ir para relatórios</Button></div></Panel></div>}
        </main>
      </div>
      <ConfirmModal
        open={!!pendingDelete}
        title="Confirmar remoção"
        description={pendingDelete ? `Tem certeza que deseja remover ${pendingDelete.name}?` : ''}
        loading={deleting}
        onCancel={() => setPendingDelete(null)}
        onConfirm={() => {
          if (!pendingDelete) return
          if (pendingDelete.type === 'colaborador') removerColaborador(pendingDelete.id)
          else removerRecurso(pendingDelete.id)
        }}
      />
    </div>
  )
}

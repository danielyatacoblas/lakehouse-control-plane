import { useEffect, useState } from 'react';
import {
  Activity, ArrowUpRight, Boxes, CircleCheck, Clock3, Command, Gauge,
  Menu, Moon, RefreshCw, Search, Server, ShieldCheck, Sun, X
} from 'lucide-react';
import { project } from './project';
import './views.css';

type ApiState = 'checking'|'online'|'offline';
type Theme = 'dark'|'light';

const icons = [Activity, Boxes, Server, ShieldCheck, Gauge];

export default function App() {
  const params = new URLSearchParams(window.location.search);
  const initialView = Math.min(Math.max(Number(params.get('view') ?? 0), 0), project.nav.length - 1);
  const [apiState,setApiState]=useState<ApiState>('checking');
  const [running,setRunning]=useState(false);
  const [message,setMessage]=useState('Datos demo listos para explorar');
  const [mobile,setMobile]=useState(false);
  const [activeView,setActiveView]=useState(initialView);
  const [theme,setTheme]=useState<Theme>(params.get('theme')==='light'?'light':'dark');

  const syncUrl=(view:number,nextTheme:Theme)=>{
    const search=new URLSearchParams(window.location.search);
    search.set('view',String(view));
    search.set('theme',nextTheme);
    window.history.replaceState({},'',`${window.location.pathname}?${search.toString()}`);
  };
  const selectView=(index:number)=>{setActiveView(index);setMobile(false);syncUrl(index,theme)};
  const toggleTheme=()=>{const next=theme==='dark'?'light':'dark';setTheme(next);syncUrl(activeView,next)};
  const checkHealth=async()=>{setApiState('checking');try{const response=await fetch(project.healthUrl);setApiState(response.ok?'online':'offline')}catch{setApiState('offline')}};
  useEffect(()=>{void checkHealth()},[]);
  const execute=async()=>{setRunning(true);setMessage('Enviando operación al backend…');try{const response=await fetch(project.action.url,{method:project.action.method,headers:{'Content-Type':'application/json',...(project.action.headers||{})},body:project.action.body?JSON.stringify(project.action.body):undefined});if(!response.ok)throw new Error(`HTTP ${response.status}`);const body=await response.json();setMessage(`Operación aceptada · ${body.id||body.runId||body.externalId||'respuesta recibida'}`);setApiState('online')}catch{setMessage('Backend local no iniciado · la consola conserva datos demo');setApiState('offline')}finally{setRunning(false)}};

  const Metrics=()=> <div className="metrics">{project.metrics.map((metric,index)=><article key={metric.label}><div className="metric-top"><span>{metric.label}</span><span className={`trend ${metric.tone||'info'}`}>{metric.trend}</span></div><strong>{metric.value}</strong><div className="spark">{[22,38,29,54,48,71,64,83].map((height,i)=><i key={i} style={{height:`${(height+index*4)%88}%`}}/>)}</div></article>)}</div>;
  const Flow=()=> <section className="panel flow-panel"><div className="panel-head"><div><span className="kicker">Flujo vivo</span><h2>Orquestación del proceso</h2></div><span className="live"><i/>LIVE</span></div><div className="flow">{project.stages.map((stage,index)=><div className="flow-step" key={stage.label}><div className="flow-icon">{index<project.stages.length-1?<CircleCheck size={18}/>:<Clock3 size={18}/>}</div><div><b>{stage.label}</b><span>{stage.detail}</span></div>{index<project.stages.length-1&&<i className="connector"/>}</div>)}</div><div className="system-message"><span><CircleCheck size={17}/></span><div><b>{message}</b><small>La vista identifica claramente cuándo usa fixtures locales.</small></div></div></section>;
  const ActivityPanel=()=> <section className="panel activity-panel"><div className="panel-head"><div><span className="kicker">Actividad</span><h2>Eventos recientes</h2></div><span className="live"><i/>LIVE</span></div><div className="activity-list">{project.activity.map((event,index)=><div className="activity-item" key={event.title}><span className={`dot d${index}`}/><div><b>{event.title}</b><p>{event.detail}</p></div><time>{event.time}</time></div>)}</div></section>;
  const OperationsTable=({title='Operaciones más recientes'}:{title?:string})=> <section className="panel table-panel"><div className="panel-head"><div><span className="kicker">Vista operativa</span><h2>{title}</h2></div><div className="chips"><button className="selected">Todas</button><button>En curso</button><button>Incidentes</button></div></div><div className="table-wrap"><table><thead><tr><th>Identificador</th><th>Entidad</th><th>Estado</th><th>Valor</th><th>Actualización</th><th/></tr></thead><tbody>{project.rows.map(row=><tr key={row.id}><td><code>{row.id}</code></td><td><b>{row.primary}</b><small>{row.secondary}</small></td><td><span className={`status ${row.status.toLowerCase().replaceAll(' ','-')}`}>{row.status}</span></td><td>{row.amount}</td><td>{row.time}</td><td><ArrowUpRight size={16}/></td></tr>)}</tbody></table></div></section>;
  const PageIntro=({index,description}:{index:number;description:string})=> <div className="page-intro"><div><span className="eyebrow">{project.eyebrow}</span><h1>{project.nav[index]}</h1><p>{description}</p></div><span className="view-badge">Vista {index+1} de {project.nav.length}</span></div>;

  const renderView=()=>{
    if(activeView===0)return <><div className="hero"><div><span className="eyebrow">{project.eyebrow}</span><h1>Control de operaciones<br/><em>en tiempo real.</em></h1><p>{project.description}</p></div><div className="hero-actions"><button className="primary" data-testid="primary-action" onClick={execute} disabled={running}>{running?'Procesando…':project.actionLabel}<ArrowUpRight size={18}/></button><small>{project.actionNote}</small></div></div><Metrics/><div className="grid-main"><Flow/><ActivityPanel/></div><OperationsTable/></>;
    if(activeView===1)return <><PageIntro index={1} description={`Consulta, filtra y supervisa ${project.nav[1].toLowerCase()} con estados y trazabilidad operacional.`}/><div className="metrics compact">{project.metrics.slice(0,3).map(metric=><article key={metric.label}><div className="metric-top"><span>{metric.label}</span><span className={`trend ${metric.tone||'info'}`}>{metric.trend}</span></div><strong>{metric.value}</strong></article>)}</div><OperationsTable title={project.nav[1]}/><section className="panel section-summary"><div><span className="kicker">Detalle operacional</span><h2>Lectura rápida por etapa</h2></div><div className="stage-cards">{project.stages.map((stage,index)=><article key={stage.label}><span>0{index+1}</span><b>{stage.label}</b><small>{stage.detail}</small></article>)}</div></section></>;
    if(activeView===2)return <><PageIntro index={2} description={`Seguimiento especializado de ${project.nav[2].toLowerCase()} y sus dependencias técnicas.`}/><Flow/><div className="grid-main lower-grid"><section className="panel"><div className="panel-head"><div><span className="kicker">Capacidad</span><h2>Indicadores por componente</h2></div></div><div className="capacity-list">{project.technology.map((item,index)=><div key={item}><span>{item}</span><i><b style={{width:`${94-index*7}%`}}/></i><strong>{94-index*7}%</strong></div>)}</div></section><ActivityPanel/></div></>;
    if(activeView===3)return <><PageIntro index={3} description={`Control de ${project.nav[3].toLowerCase()}, excepciones y decisiones que requieren seguimiento.`}/><div className="grid-main"><ActivityPanel/><section className="panel decision-panel"><div className="panel-head"><div><span className="kicker">Decisiones</span><h2>Cola priorizada</h2></div><span className="view-badge">SLA activo</span></div>{project.rows.slice(0,3).map((row,index)=><article key={row.id}><span className={`dot d${index}`}/><div><b>{row.id} · {row.primary}</b><small>{row.status} · {row.time}</small></div><button>Revisar</button></article>)}</section></div><OperationsTable title={project.nav[3]}/></>;
    return <><PageIntro index={4} description={`Gobierno, salud técnica y trazabilidad de ${project.nav[4].toLowerCase()}.`}/><Metrics/><section className="panel health-panel"><div className="panel-head"><div><span className="kicker">Plataforma</span><h2>Salud de componentes</h2></div><span className="live"><i/>OPERATIVO</span></div><div className="health-grid">{project.technology.map((item,index)=><article key={item}><CircleCheck size={20}/><div><b>{item}</b><small>{index===0?'Runtime verificado':'Componente supervisado'}</small></div><span>{index===2?'99.95%':'OK'}</span></article>)}</div></section><div className="grid-main lower-grid"><Flow/><ActivityPanel/></div></>;
  };

  return <div className={`app ${theme}`} data-view={activeView} style={{'--accent':project.accent} as React.CSSProperties}>
    <aside className={mobile?'sidebar open':'sidebar'}>
      <button className="close" aria-label="Cerrar menú" onClick={()=>setMobile(false)}><X/></button>
      <div className="brand"><span className="brand-mark">{project.initials}</span><div><b>{project.name}</b><small>Operations</small></div></div>
      <nav>{project.nav.map((item,index)=>{const Icon=icons[index]??Server;return <button className={index===activeView?'active':''} aria-current={index===activeView?'page':undefined} onClick={()=>selectView(index)} key={item}><span><Icon size={17}/></span>{item}</button>})}</nav>
      <div className="sidebar-card"><Command size={19}/><b>Java Platform</b><p>Cinco vistas operativas con evidencia reproducible.</p><span>release {project.release ?? 'portfolio'}</span></div>
      <div className="profile"><span>AR</span><div><b>Andrea R.</b><small>Platform operator</small></div></div>
    </aside>
    <main>
      <header><button className="menu" aria-label="Abrir menú" onClick={()=>setMobile(true)}><Menu/></button><div className="search"><Search size={17}/><span>Buscar por identificador, estado o cliente…</span><kbd>⌘ K</kbd></div><div className={`connection ${apiState}`}><i/>{apiState==='online'?'API conectada':apiState==='checking'?'Comprobando':'Modo demostración'}</div><button className="icon-button" aria-label={theme==='dark'?'Activar tema claro':'Activar tema oscuro'} onClick={toggleTheme}>{theme==='dark'?<Sun size={18}/>:<Moon size={18}/>}</button><button className="icon-button" aria-label="Actualizar" onClick={checkHealth}><RefreshCw size={18}/></button></header>
      <section className="content">{renderView()}<footer><span>Stack verificado</span>{project.technology.map(item=><b key={item}>{item}</b>)}<small>Datos sintéticos · Portafolio técnico</small></footer></section>
    </main>
  </div>
}

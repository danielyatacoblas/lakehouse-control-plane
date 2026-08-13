import { useEffect, useState } from 'react';
import { Activity, ArrowUpRight, Boxes, CircleCheck, Clock3, Command, Menu, RefreshCw, Search, Server, X } from 'lucide-react';
import { project } from './project';

type ApiState = 'checking'|'online'|'offline';

export default function App() {
  const [apiState,setApiState]=useState<ApiState>('checking');
  const [running,setRunning]=useState(false);
  const [message,setMessage]=useState('Datos demo listos para explorar');
  const [mobile,setMobile]=useState(false);
  const checkHealth=async()=>{setApiState('checking');try{const response=await fetch(project.healthUrl);setApiState(response.ok?'online':'offline')}catch{setApiState('offline')}};
  useEffect(()=>{void checkHealth()},[]);
  const execute=async()=>{setRunning(true);setMessage('Enviando operación al backend…');try{const response=await fetch(project.action.url,{method:project.action.method,headers:{'Content-Type':'application/json',...(project.action.headers||{})},body:project.action.body?JSON.stringify(project.action.body):undefined});if(!response.ok)throw new Error(`HTTP ${response.status}`);const body=await response.json();setMessage(`Operación aceptada · ${body.id||body.runId||body.externalId||'respuesta recibida'}`);setApiState('online')}catch{setMessage('Backend local no iniciado · la consola conserva datos demo');setApiState('offline')}finally{setRunning(false)}};

  return <div className="app" style={{'--accent':project.accent} as React.CSSProperties}>
    <aside className={mobile?'sidebar open':'sidebar'}>
      <button className="close" aria-label="Cerrar menú" onClick={()=>setMobile(false)}><X/></button>
      <div className="brand"><span className="brand-mark">{project.initials}</span><div><b>{project.name}</b><small>Operations</small></div></div>
      <nav>{project.nav.map((item,index)=><button className={index===0?'active':''} key={item}><span>{index===0?<Activity size={17}/>:index===1?<Boxes size={17}/>:<Server size={17}/>}</span>{item}</button>)}</nav>
      <div className="sidebar-card"><Command size={19}/><b>Java Platform</b><p>Observabilidad y operaciones en una sola vista.</p><span>release {project.release ?? 'v0.2.2'}</span></div>
      <div className="profile"><span>AR</span><div><b>Andrea R.</b><small>Platform operator</small></div></div>
    </aside>
    <main>
      <header><button className="menu" aria-label="Abrir menú" onClick={()=>setMobile(true)}><Menu/></button><div className="search"><Search size={17}/><span>Buscar por identificador, estado o cliente…</span><kbd>⌘ K</kbd></div><div className={`connection ${apiState}`}><i/>{apiState==='online'?'API conectada':apiState==='checking'?'Comprobando':'Modo demostración'}</div><button className="icon-button" aria-label="Actualizar" onClick={checkHealth}><RefreshCw size={18}/></button></header>
      <section className="content">
        <div className="hero"><div><span className="eyebrow">{project.eyebrow}</span><h1>Control de operaciones<br/><em>en tiempo real.</em></h1><p>{project.description}</p></div><div className="hero-actions"><button className="primary" data-testid="primary-action" onClick={execute} disabled={running}>{running?'Procesando…':project.actionLabel}<ArrowUpRight size={18}/></button><small>{project.actionNote}</small></div></div>
        <div className="metrics">{project.metrics.map((metric,index)=><article key={metric.label}><div className="metric-top"><span>{metric.label}</span><span className={`trend ${metric.tone||'info'}`}>{metric.trend}</span></div><strong>{metric.value}</strong><div className="spark">{[22,38,29,54,48,71,64,83].map((height,i)=><i key={i} style={{height:`${(height+index*4)%88}%`}}/>)}</div></article>)}</div>
        <div className="grid-main">
          <section className="panel flow-panel"><div className="panel-head"><div><span className="kicker">Flujo vivo</span><h2>Orquestación del proceso</h2></div><span className="live"><i/>LIVE</span></div><div className="flow">{project.stages.map((stage,index)=><div className="flow-step" key={stage.label}><div className="flow-icon">{index<project.stages.length-1?<CircleCheck size={18}/>:<Clock3 size={18}/>}</div><div><b>{stage.label}</b><span>{stage.detail}</span></div>{index<project.stages.length-1&&<i className="connector"/>}</div>)}</div><div className="system-message"><span><CircleCheck size={17}/></span><div><b>{message}</b><small>La vista identifica claramente cuándo usa fixtures locales.</small></div></div></section>
          <section className="panel activity-panel"><div className="panel-head"><div><span className="kicker">Actividad</span><h2>Eventos recientes</h2></div><button>Ver todo</button></div><div className="activity-list">{project.activity.map((event,index)=><div className="activity-item" key={event.title}><span className={`dot d${index}`}/><div><b>{event.title}</b><p>{event.detail}</p></div><time>{event.time}</time></div>)}</div></section>
        </div>
        <section className="panel table-panel"><div className="panel-head"><div><span className="kicker">Vista operativa</span><h2>Operaciones más recientes</h2></div><div className="chips"><button className="selected">Todas</button><button>En curso</button><button>Incidentes</button></div></div><div className="table-wrap"><table><thead><tr><th>Identificador</th><th>Entidad</th><th>Estado</th><th>Valor</th><th>Actualización</th><th/></tr></thead><tbody>{project.rows.map(row=><tr key={row.id}><td><code>{row.id}</code></td><td><b>{row.primary}</b><small>{row.secondary}</small></td><td><span className={`status ${row.status.toLowerCase().replaceAll(' ','-')}`}>{row.status}</span></td><td>{row.amount}</td><td>{row.time}</td><td><ArrowUpRight size={16}/></td></tr>)}</tbody></table></div></section>
        <footer><span>Stack verificado</span>{project.technology.map(item=><b key={item}>{item}</b>)}<small>Datos sintéticos · Portafolio técnico</small></footer>
      </section>
    </main>
  </div>
}

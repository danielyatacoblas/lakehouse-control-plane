export type Metric = { label: string; value: string; trend: string; tone?: 'good'|'warn'|'info' };
export type Row = { id: string; primary: string; secondary: string; status: string; amount: string; time: string };
export type Project = {
  name: string; release?: string; eyebrow: string; description: string; accent: string; initials: string;
  nav: string[]; metrics: Metric[]; stages: {label:string; detail:string}[]; rows: Row[];
  actionLabel: string; actionNote: string; healthUrl: string;
  action: { url: string; method: string; headers?: Record<string,string>; body?: unknown };
  technology: string[]; activity: {title:string; detail:string; time:string}[];
};

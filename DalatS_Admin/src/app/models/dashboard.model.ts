export interface DashboardSummary {
  tongSuCo: number;
  choXuLy: number;
  dangXuLy: number;
  daHoanThanh: number;
  tuChoi: number;
}

export interface IncidentByAlert {
  alertName: string;
  count: number;
}

export interface IncidentByCategory {
  categoryName: string;
  count: number;
}
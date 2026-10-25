export interface ResourceCategory {
  id: string;
  categoryTitle: string;
  description: string;
  items: ResourceItem[];
}

export interface ResourceItem {
  title: string;
  icon: string;
  description: string;
  
  actionText?: string;
  url?: string;
  label?: string;
  badgeClass?: string;
  location: string;
  contact?: string;
}
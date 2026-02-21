export interface ResourceCategory {
  id: string;
  category: string;
  icon: string;
  description: string;
  resources: ResourceItem[];
}

export interface ResourceItem {
  name: string;
  description: string;
}
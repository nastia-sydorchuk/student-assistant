export interface CampusLife {
  id: string;
  category: string;
  icon: string;
  description: string;
  items: LifeItem[];
}

export interface LifeItem {
  name: string;
  description: string;
  highlight: string;
}
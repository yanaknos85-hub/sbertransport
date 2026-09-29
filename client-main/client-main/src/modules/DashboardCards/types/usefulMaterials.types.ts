interface TInfo {
  type: string;
  text: string;
}

interface TPage {
  image: string;
  title: string;
  text: string;
  type: string;
  info?: TInfo;
}

export interface IInstruction {
  name: string;
  coverImage: string;
  title: string;
  pages: TPage[];
}

export interface IServices {
  serviceName: string;
  serviceCoverImage: string;
  serviceTitle: string;
  instructions: IInstruction[];
}

export interface IInstructions {
  services: IServices[];
  version: string;
}

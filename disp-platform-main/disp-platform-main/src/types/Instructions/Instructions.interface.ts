interface TPage {
  image: string;
  title: string;
  text: string;
}

export interface IInstruction {
  name: string;
  coverImage: string;
  title: string;
  pages: TPage[];
  link?: string;
}

export interface IInstructions {
  title: string;
  instructions: IInstruction[];
}

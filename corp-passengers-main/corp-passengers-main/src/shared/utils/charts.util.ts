export enum AnalyticTypes {
  total = 'total',
  csi = 'CSI',
  sla = 'SLA',
  budget = 'budget',
}

enum Colors {
  green = '#10BF6A',
  greenLight = '#CFF2E1',
  gray = '#C2C2C2',
  grayLight = '#EBEBEB',
  orange = '#FFC914',
  red = '#FF5743',
  redLight = '#FFDDD9',
}

interface ColorDataType {
  innerColors: Colors[];
  outerColors: Colors[];
  legendColors: Colors[];
}

export const getColors = (value: number, type: string, normal = 95): ColorDataType | never => {
  const getColorscheme: (() => ColorDataType) | undefined = {
    [AnalyticTypes.total]: () => ({
      innerColors: [Colors.green, Colors.orange],
      outerColors: [Colors.gray],
      legendColors: [Colors.green, Colors.orange],
    }),
    [AnalyticTypes.budget]: () => ({
      innerColors: [Colors.green, Colors.orange],
      outerColors: [Colors.gray],
      legendColors: [Colors.green, Colors.orange, Colors.gray],
    }),
    [AnalyticTypes.csi]: () => {
      const primaryColor = value >= normal ? Colors.green : Colors.red;
      return {
        innerColors: [primaryColor, Colors.grayLight],
        outerColors: [Colors.grayLight, Colors.greenLight],
        legendColors: [primaryColor, Colors.greenLight],
      };
    },
    [AnalyticTypes.sla]: () => {
      const primaryColor = value >= normal ? Colors.green : Colors.red;

      return {
        innerColors: [primaryColor, Colors.grayLight],
        outerColors: [Colors.grayLight, Colors.greenLight],
        legendColors: [primaryColor, Colors.greenLight],
      };
    },
  }[type as AnalyticTypes];

  if (!getColorscheme) {
    throw new Error('Invalid color scheme type');
  }

  return getColorscheme();
};

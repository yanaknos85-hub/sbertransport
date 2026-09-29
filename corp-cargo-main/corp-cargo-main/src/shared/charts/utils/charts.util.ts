import { Colors, AnalyticTypes } from '../constants/charts.constants';

interface ColorDataType {
  innerColors: Colors[];
  outerColors: Colors[];
  legendColors: Colors[];
}

export const getColors = (value: number, type: string, normal = 95): ColorDataType | never => {
  const getColorscheme: (() => ColorDataType) | undefined = {
    [AnalyticTypes.total]: () => ({
      innerColors: [Colors.green, Colors.orangeLight, Colors.grayLight],
      outerColors: [Colors.gray],
      legendColors: [Colors.green, Colors.orangeLight, Colors.grayLight],
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

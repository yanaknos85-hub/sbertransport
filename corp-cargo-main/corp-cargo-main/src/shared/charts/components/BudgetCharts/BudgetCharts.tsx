import React from 'react';
import {
  PieChart, Pie, Cell, Legend, ResponsiveContainer
} from 'recharts';
import cn from 'classnames';
import { formatRublesWithoutPennies, convertToRubles } from 'utils';
import { getColors } from '../../utils/charts.util';
import { IDataTotal, ILimitsResponseTotal } from '../../types/types';
import { convertNumberToPercent } from '../../utils/convertNumberToPercent.util';

import * as Styles from '../Card/Card.styled';
import styles from './BudgetCharts.module.scss';

const renderCustomLegend = (data: IDataTotal[], type: string): JSX.Element => (
  <div className={styles.legendWrapper}>
    {data.map((entry, index: number) => (
      <div key={entry.name} className={styles.customLegend}>
        <Styles.Cyrcle color={getColors(entry.value, type).legendColors[index]} />
        <span className="recharts-legend-item-text">{entry.name}</span>
      </div>
    ))}
  </div>
);

const BudgetCharts = ({ data }: { data: ILimitsResponseTotal }): JSX.Element => {
  const { type } = data;
  const [response] = data.data;
  const { totalBudget, spentBudget } = response;

  // const totalBudget = 120000;
  // const spentBudget = 80000;

  const balanceBudget = totalBudget - spentBudget;

  const dataForPie = [
    {
      ...response,
      name: 'Остаток',
      value: balanceBudget,
      valueForPie: convertNumberToPercent(totalBudget, balanceBudget),
    },
    {
      ...response,
      name: 'Потрачено',
      value: spentBudget,
      valueForPie: convertNumberToPercent(totalBudget, spentBudget),
    },
  ];

  const dataForLegend = [
    ...dataForPie,
    {
      ...response,
      name: 'Всего',
      value: totalBudget,
    },
  ];

  const labels = [balanceBudget, spentBudget];

  const dataForOuterPie = [{ value: 100 }];

  return (
    <>
      <div className={styles.wrapper}>
        <ResponsiveContainer width="100%" height="100%">
          <PieChart width={200} height={200}>
            <Pie
              data={dataForPie}
              dataKey="valueForPie"
              labelLine={false}
              cx="50%"
              cy="50%"
              innerRadius="60%"
              outerRadius="88%"
              startAngle={240}
              endAngle={-60}
            >
              {/* <Label value={formatValue(dataForPie[0].value, dataForPie[0].typeValue)} position="center" /> */}
              {dataForPie.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={getColors(entry.value, type).innerColors[index]} />
              ))}
            </Pie>
            <Pie
              data={dataForOuterPie}
              labelLine={false}
              dataKey="value"
              cx="50%"
              cy="50%"
              innerRadius="88%"
              outerRadius="93%"
              startAngle={240}
              endAngle={-60}
            >
              {dataForPie.map((entry, index) => (
                <Cell
                  key={`cell-${index}`}
                  fill={
                    getColors(entry.value, type).outerColors[index % getColors(entry.value, type).outerColors.length]
                  }
                />
              ))}
            </Pie>
          </PieChart>
        </ResponsiveContainer>
        <div className={cn(styles.labels, { [styles[`labels-${type}`]]: type })}>
          {labels.map((label, i) => (
            <p
              className={cn(styles.customLabel, {
                [styles.customLabelOrange]: i === 1,
              })}
            >
              {formatRublesWithoutPennies(convertToRubles(label) / 1000, true)}
            </p>
          ))}
        </div>

        <p className={styles.outerLegend}>{formatRublesWithoutPennies(convertToRubles(totalBudget) / 1000, true)}</p>
      </div>

      <div className={styles.legend}>
        <Legend
          style={{ marginTop: '16px' }}
          align="left"
          content={renderCustomLegend(dataForLegend, type)}
        />
      </div>
    </>
  );
};

export default BudgetCharts;

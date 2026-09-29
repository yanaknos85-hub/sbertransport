import React from 'react';
import {
  PieChart, Pie, Cell, Legend, ResponsiveContainer
} from 'recharts';
import { useTranslation } from 'i18n';
import cn from 'classnames';
import { IDataTotal, IResponseTotal } from '../../types/types';
import { Colors } from '../../constants/charts.constants';
import { getColors } from '../../utils/charts.util';
import { formatDataForPieChart, formatValue, serializeName } from '../../utils/formaters.util';

import * as Styles from '../Card/Card.styled';
import styles from './ServiceCharts.module.scss';

const CustomLegend = ({ data, type }: { data: IDataTotal[]; type: string }): JSX.Element => {
  const { t } = useTranslation();
  const isTotal = type === 'total';

  return (
    <div className={cn('', { [styles.legendWrapper]: isTotal })}>
      {data.map((entry, index: number) => (
        <div key={entry.name} className={cn(styles.customLegend, { [styles.customWrapLegend]: isTotal })}>
          <Styles.Cyrcle color={getColors(entry.value, type).legendColors[index]} />
          <span className="recharts-legend-item-text">
            {serializeName(entry.name, t)}
            {' '}
            {entry.name === t.Widgets.Legends.norm && formatValue(entry.value, entry.typeValue)}
          </span>
        </div>
      ))}
      {type === 'total' && (
        <div className={cn(styles.customLegend, { [styles.customWrapLegend]: isTotal })}>
          <Styles.Cyrcle color={Colors.gray} />
          <span className="recharts-legend-item-text">{t.Widgets.Legends.total}</span>
        </div>
      )}
    </div>
  );
};

const ServiceCharts = ({ data }: { data: IResponseTotal }): JSX.Element => {
  const { type } = data;
  const dataForPie = formatDataForPieChart(data.data, type);

  const isTotal = type === 'total';
  const defaultNormValue = 95;
  const dataForOuterPie = isTotal ? [{ value: 100 }] : [{ value: 100 }, { value: 5 }];

  const [executed, notExecuted, cancelled] = dataForPie;

  const formattedExecutedValue = formatValue(executed.value, executed.typeValue);
  const formattedNotExecutedValue = formatValue(notExecuted.value, notExecuted.typeValue);

  const labels = [formattedExecutedValue];

  if (isTotal) {
    labels.push(formattedNotExecutedValue, formatValue(cancelled.value, cancelled.typeValue));
  }

  const isRedLabel = executed.value < defaultNormValue;

  const outerFormatLegendValue = isTotal
    ? executed.value + notExecuted.value + cancelled.value
    : formattedNotExecutedValue;

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
              key={`${label}_${i}`}
              className={cn(styles.customLabel, {
                [styles.customLabelRed]: !isTotal && isRedLabel,
                [styles.customLabelOrangeLight]: isTotal && i === 1,
                [styles.customLabelGrayLight]: isTotal && i === 2,
              })}
            >
              {label}
            </p>
          ))}
        </div>

        <p
          className={cn(styles.outerLegend, {
            [styles.outerLegendTotal]: isTotal,
          })}
        >
          {outerFormatLegendValue}
        </p>
      </div>
      <div className={styles.legend}>
        <Legend
          style={{ marginTop: '16px' }}
          align="left"
          content={<CustomLegend data={dataForPie} type={type} />}
        />
      </div>
    </>
  );
};

export default ServiceCharts;

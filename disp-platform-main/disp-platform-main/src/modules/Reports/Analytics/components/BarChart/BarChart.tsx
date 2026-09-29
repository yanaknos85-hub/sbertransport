import React, {
  FC, useMemo, useCallback, useState, useRef, useEffect
} from 'react';
import moment from 'moment';

import { makeFirstFoundCharUppercase, monthNamesShort } from 'utils/calendar';
import { BarChartProps } from './BarChart.types';
import { calculateRangeWidth } from './BarChart.utils';
import styles from './BarChart.module.scss';

const defaultChartMargin = {
  top: 20,
  right: 0,
  bottom: 20,
  left: 0,
};

const BarChart: FC<BarChartProps> = ({
  data,
  period,
  multiple = false,
  separate = false,
  width = 700,
  height = 126,
  barWidth = 50,
  spacing = 8,
  minRangeWidth = 50,
  primaryColor = '#a9de59',
  secondaryColor = '#e28965',
  currentBarColor = '#f3f5f7',
  rangeSign = 'шт.',
  filters = { showPrimary: true, showSecondary: true },
  tooltipProps,
  showBarValue,
  currentBarWidth = 57,
  separatePadding = 8,
  externalPadding = 16,
  showCurrent = true,
  chartMargin = defaultChartMargin,
  rangeMargin = 5,
}) => {
  const [mousePosition, setMousePosition] = useState({ x: 0, y: 0 });
  const [activeIndex, setActiveIndex] = useState<number | null>(null);
  const [containerWidth, setContainerWidth] = useState(width);

  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const resize = () => {
      const widthOffset = containerRef.current?.getBoundingClientRect().width ?? width;
      setContainerWidth(widthOffset - externalPadding);
    };

    setTimeout(resize, 200);
    window.addEventListener('resize', resize);

    return () => {
      window.removeEventListener('resize', resize);
    };
  }, [externalPadding, width]);

  const maxValue = useMemo(() => {
    if (!data.length) return 0;
    return Math.max(
      ...data.map(datum => (filters.showPrimary ? datum.primary + (filters.showSecondary ? datum.secondary : 0) : 0)),
      ...data.map(datum => (filters.showSecondary ? datum.secondary : 0))
    );
  }, [data, filters]);

  const rangeWidth = useMemo(() => {
    return calculateRangeWidth({
      rangeSign,
      maxValue,
      minRangeWidth,
    });
  }, [maxValue, minRangeWidth, rangeSign]);

  const scaleMarks = useMemo(() => [...Array(3)].map((_, idx) => (idx * maxValue) / 2), [maxValue]);

  const spacePerUnit = useMemo(() => {
    const numBars = data.length;
    const availableSpace = containerWidth - (rangeWidth + rangeMargin);

    return availableSpace / (numBars + (numBars > 1 ? numBars - 1 : 0));
  }, [containerWidth, rangeWidth, rangeMargin, data.length]);

  const yScale = useCallback(
    value => height
    - chartMargin.bottom
    - (height - chartMargin.top - chartMargin.bottom) * (value / maxValue),
    [height, maxValue, chartMargin]
  );

  const renderScaleMarks = useCallback(() => {
    if (scaleMarks.every(mark => mark === 0)) {
      return (
        <span key="zero-mark" className={styles.scaleMark}>
          0
        </span>
      );
    }

    return scaleMarks.map((mark, i) => (
      <span key={`mark-${i}`} className={styles.scaleMark}>
        {Math.round(mark)}
        {mark !== 0 && ` ${rangeSign}`}
      </span>
    ));
  }, [scaleMarks, rangeSign]);

  const handleMouseMove = useCallback(event => {
    setMousePosition({ x: event.clientX, y: event.clientY });
  }, []);

  const renderTooltipItem = useCallback((background, label, value, fontWeight = 500) => (
    <div
      className={styles.item}
      key={label}
    >
      {!!background && <span className={styles.point} style={{ background }} />}
      <span className={styles.text} style={{ fontWeight }}>
        {label}
        {!!background && ':' /* По дизайну сейчас без бэкграунда двоеточия нет, хоть условие и выглядит странным */}
      </span>
      <span className={styles.text} style={{ fontWeight }}>
        {value}
        {' '}
        {tooltipProps?.rangeSign ?? rangeSign}
      </span>
    </div>
  ), [tooltipProps?.rangeSign, rangeSign]);

  return (
    <div
      className={styles.container}
      style={{ minWidth: width + (rangeWidth - minRangeWidth) }}
      ref={containerRef}
      data-testid="bar-chart-container"
    >
      <div className={styles.wrapper}>
        <div className={styles.range} style={{ width: rangeWidth, top: chartMargin.top }}>
          {renderScaleMarks()}
        </div>

        <svg
          width="100%"
          data-testid="bar-chart-svg"
          height={height}
          onMouseMove={tooltipProps ? handleMouseMove : undefined}
        >
          {data.map((datum, index) => {
            let primaryRect, secondaryRect, currentRect;

            const xOffset = rangeWidth + index * (
              (multiple && separate ? spacePerUnit * 2 : spacePerUnit + spacePerUnit)
            );
            const isDayPeriod = period === 'day';

            const isCurrentPeriod = showCurrent
              ? moment()
                .set(isDayPeriod ? 'date' : 'month', datum.date - (isDayPeriod ? 0 : 1))
                .isSame(moment(), isDayPeriod ? 'date' : 'month') && (datum.selectedMonth !== undefined ? moment().month() === datum.selectedMonth : true)
              : null;

            if (multiple && separate) {
              const columnWidth = barWidth / 2 - separatePadding - spacing / 2;

              const primaryY = yScale(datum.primary);
              const primaryHeight = height - chartMargin.bottom - primaryY;

              const secondaryY = yScale(datum.secondary);
              const secondaryHeight = height - chartMargin.bottom - secondaryY;

              const startPoint = xOffset - separatePadding - spacing;

              primaryRect = {
                x: startPoint + separatePadding,
                y: primaryY,
                width: columnWidth,
                height: primaryHeight,
                color: primaryColor,
              };

              secondaryRect = {
                x: startPoint + separatePadding + columnWidth + spacing,
                width: columnWidth,
                height: secondaryHeight,
                y: secondaryY,
                color: secondaryColor,
              };

              if (isCurrentPeriod) {
                currentRect = {
                  x: xOffset - (currentBarWidth - barWidth) / 2 - separatePadding - spacing,
                };
              }
            } else {
              const primaryY = yScale(multiple && filters.showSecondary
                ? datum.primary + datum.secondary
                : datum.primary
              );
              const primaryHeight = height - chartMargin.bottom - primaryY;

              primaryRect = {
                x: xOffset,
                y: primaryY,
                width: barWidth,
                height: primaryHeight,
                color: primaryColor,
              };

              if (multiple) {
                const secondaryY = yScale(datum.secondary);
                const secondaryHeight = height - chartMargin.bottom - secondaryY;

                secondaryRect = {
                  x: xOffset,
                  y: secondaryY,
                  width: barWidth,
                  height: secondaryHeight,
                  color: secondaryColor,
                };
              }

              if (isCurrentPeriod) {
                currentRect = {
                  x: xOffset - (currentBarWidth - barWidth) / 2,
                };
              }
            }

            return (
              <g
                key={index}
                onMouseEnter={tooltipProps ? () => setActiveIndex(index) : undefined}
                onMouseLeave={tooltipProps ? () => setActiveIndex(null) : undefined}
              >
                {isCurrentPeriod && currentRect && (
                  <rect
                    {...currentRect}
                    y={0}
                    width={currentBarWidth}
                    height={height}
                    key={`current-bar-${index}`}
                    fill={currentBarColor}
                    rx="8"
                    ry="8"
                  />
                )}
                {filters.showPrimary && primaryRect && (
                  <rect
                    {...primaryRect}
                    fill={primaryRect.color}
                    rx="4"
                    ry="4"
                  />
                )}
                {filters.showSecondary && secondaryRect && (
                  <rect
                    {...secondaryRect}
                    fill={secondaryRect.color}
                    rx="4"
                    ry="4"
                  />
                )}
                {showBarValue && filters.showPrimary && (
                  <text
                    x={xOffset + barWidth / 2}
                    y={primaryRect.y - 4}
                    className={styles.text}
                  >
                    {datum.primary + (multiple && filters.showPrimary ? datum.secondary : 0) || '-'}
                  </text>
                )}
                {showBarValue && multiple && !filters.showPrimary && filters.showSecondary && (
                  <text
                    x={xOffset + barWidth / 2}
                    y={secondaryRect.y - 4}
                    className={styles.text}
                  >
                    {datum.secondary || '-'}
                  </text>
                )}
                <text
                  x={xOffset + barWidth / 2 - (multiple && separate ? separatePadding : 0)}
                  y={height - 4}
                  textAnchor="middle"
                  className={styles.periodLabel}
                >
                  {period === 'day' ? datum.date : monthNamesShort[datum.date - 1]}
                </text>
              </g>
            );
          })}
        </svg>
      </div>

      {typeof activeIndex === 'number' && !!tooltipProps && (
        <div
          className={styles.tooltip}
          style={{
            left: mousePosition.x + 10,
            top: mousePosition.y - 20,
          }}
        >
          {tooltipProps.year && (
            <div className={styles.date}>
              {makeFirstFoundCharUppercase(
                moment([
                  tooltipProps.year,
                  ...(typeof tooltipProps.month === 'number' ? [tooltipProps.month] : []),
                  activeIndex + (typeof tooltipProps.month === 'number' ? 1 : 0),
                ]).format(typeof tooltipProps.month === 'number' ? 'DD MMMM YYYY' : 'MMMM YYYY')
              )}
            </div>
          )}
          {tooltipProps.content?.(activeIndex)}

          {tooltipProps.list?.(activeIndex)?.map(item => renderTooltipItem(
            item.background, item.label, item.value, item.fontWeight
          ))}

          {tooltipProps.primaryText && renderTooltipItem(
            primaryColor, tooltipProps.primaryText, data[activeIndex].primary
          )}

          {tooltipProps.secondaryText && renderTooltipItem(
            secondaryColor, tooltipProps.secondaryText, data[activeIndex].secondary
          )}
        </div>
      )}
    </div>
  );
};

export default BarChart;

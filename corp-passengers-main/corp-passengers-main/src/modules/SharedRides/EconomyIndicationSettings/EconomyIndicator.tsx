import React, {
  FC, Suspense, useEffect, useRef
} from 'react';
import { Slider } from 'antd';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { EconomyIndicatorProps } from './types';

import styles from './EconomyIndicator.module.scss';

export const EconomyIndicationSettings: FC<EconomyIndicatorProps> = ({
  initialRangeValue,
  title,
  value,
  setRangeValue,
  busy,
}) => {
  const leftPartRef = useRef<HTMLDivElement>(null);
  const centerPartRef = useRef<HTMLDivElement>(null);
  const rightPartRef = useRef<HTMLDivElement>(null);
  const leftValueRef = useRef<HTMLDivElement>(null);
  const rightValueRef = useRef<HTMLDivElement>(null);

  const recalculateColorBars = (value: [number, number]) => {
    const sliderWidth = 380 - 104; // меджик намбер. Длина слайдера = ширина контейнера минус паддинги, подписи и проч
    const [lowerBorder, upperBorder] = value;
    leftPartRef.current!.style.flexGrow = `${lowerBorder / 100}`;
    centerPartRef.current!.style.flexGrow = `${(upperBorder - lowerBorder) / 100}`;
    rightPartRef.current!.style.flexGrow = `${(100 - upperBorder) / 100}`;
    leftValueRef.current!.style.left = `${(lowerBorder / 100) * sliderWidth - 4}px`;
    rightValueRef.current!.style.left = `${(upperBorder / 100) * sliderWidth - 4}px`;
  };

  const onChange = (value: [number, number]) => {
    setRangeValue(value);
    recalculateColorBars(value);
  };

  useEffect(() => {
    recalculateColorBars(value);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <ErrorBoundary>
      <Suspense fallback={<SpinWrapped />}>
        <div className={styles.economyIndicationSettings}>
          <div
            className={styles.indicatorTitle}
            onClick={() => {
              // уточнить нужен ли ресет - сделать кнопку?
              setRangeValue(initialRangeValue);
              recalculateColorBars(initialRangeValue);
            }}
          >
            {title}
          </div>

          <div className={styles.sliderContainer}>
            <div className={styles.rangeZero}>0</div>
            <div className={styles.wraper}>
              <Slider
                range
                value={value}
                onChange={onChange}
                className={styles.slider}
                tooltipVisible={false}
              />
              <div ref={leftValueRef} className={styles.leftValue}>
                {value[0]}
              </div>
              <div ref={rightValueRef} className={styles.rightValue}>
                {value[1]}
              </div>
              <div ref={leftPartRef} className={styles.leftPart} />
              <div ref={centerPartRef} className={styles.centerPart} />
              <div ref={rightPartRef} className={styles.rightPart} />
            </div>
            <div className={styles.rangeFull}>100</div>
          </div>
        </div>
        {busy && <SpinWrapped mask />}
      </Suspense>
    </ErrorBoundary>
  );
};

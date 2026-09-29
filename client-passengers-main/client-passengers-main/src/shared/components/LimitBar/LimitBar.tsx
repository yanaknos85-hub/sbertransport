/* eslint-disable @stylistic/implicit-arrow-linebreak */
import { Button, Progress } from 'antd';
import * as R from 'ramda';
import React, { FC } from 'react';
import NumberFormat from 'react-number-format';

import { useGetLimitColorsPercentInfo } from 'api/limit-settings';

import { RUBLE_SIGN } from 'constants/constants.app';

import { LimitRequestModal } from 'modules/EmployeeApp/components/LimitsPage/LimitRequestModal/LimitRequestModal';

import { LimitColorsPercentModel } from 'stores/Limits/Models/LimitColorsPercent.model';

import { LIMIT_TYPE } from '../../../stores/Limits/Limit.interface';
import { useAppStoreContext } from '../../hooks/useEmpContext';

import styles from './bar.module.scss';

interface LimitBarProps {
  name?: string | JSX.Element;
  showInfo?: boolean;
  percent?: number;
  value?: number;
  className?: string;
  limitType: LIMIT_TYPE;
}

const checkCondition = (item: LimitColorsPercentModel, limitType: string): boolean =>
  // checks whether name starts with 'dep' ('emp') or ends with 'target'
  !!item.name?.toLowerCase().startsWith(limitType) && !item.name?.toLowerCase().endsWith('target');

const filterColorsValues = (
  limitType: LIMIT_TYPE,
  limitColorsPercentValues: LimitColorsPercentModel[]
): LimitColorsPercentModel[] => {
  let currentTypeColorsPercentValues: LimitColorsPercentModel[] = [];

  if (limitType === LIMIT_TYPE.EMPLOYEE) {
    currentTypeColorsPercentValues = limitColorsPercentValues.filter(element => checkCondition(element, 'emp'));
  } else if (limitType === LIMIT_TYPE.DEPARTMENT) {
    currentTypeColorsPercentValues = limitColorsPercentValues.filter(element => checkCondition(element, 'dep'));
  }
  return currentTypeColorsPercentValues;
};

const chooseColorByPercent = (percent: number, colorsObjectSpecified: any): string => {
  // FIXME sonarjs/cognitive-complexity
  // we round float number to the most closest integer number below
  const roundedPercent: number = Math.floor(percent);
  // if start and end values are the same
  // we disable these colors from the checking below
  const isAllGreen: boolean = +colorsObjectSpecified.GREEN_FROM === 0 && +colorsObjectSpecified.GREEN_UNTIL === 100;
  const isAllYellow: boolean = +colorsObjectSpecified.YELLOW_FROM === 0 && +colorsObjectSpecified.YELLOW_UNTIL === 100;
  const isAllRed: boolean = +colorsObjectSpecified.RED_FROM === 0 && +colorsObjectSpecified.RED_UNTIL === 100;
  // we save yellow color boundaries to manage color indication
  const yellowStart: number = +colorsObjectSpecified.YELLOW_FROM;
  const yellowEnd: number = +colorsObjectSpecified.YELLOW_UNTIL;
  // we disable yellow color if these values are equal
  const isYellowDisabled: boolean = yellowStart === yellowEnd;
  // color choosing - the order of expressions is important - do not change!
  const green = 'var(--green-color)';
  const yellow = 'var(--yellow-color)';
  const red = 'var(--red-color)';
  return (
    (isAllGreen && green)
    || (isAllYellow && yellow)
    || (isAllRed && red)
    // this case matches if yellow color is disabled but green and red are enabled
    || (isYellowDisabled && roundedPercent <= yellowStart && red)
    || (isYellowDisabled && roundedPercent >= yellowEnd && green)
    // this case matches if red color is disabled but yellow and green are enabled
    || (!yellowStart && roundedPercent <= yellowEnd && yellow)
    // standard case if we have all three colors enabled
    || (roundedPercent <= yellowStart && red)
    || (roundedPercent > yellowStart && roundedPercent <= yellowEnd && yellow)
    || (roundedPercent >= yellowEnd && green)
    // default color if no one above is chosen
    || 'gray'
  );
};

export const LimitBar: FC<
  LimitBarProps & { transportType?: string; showRubles?: boolean; isDepartmentHead?: boolean }
> = ({
  percent = 0,
  name,
  value,
  showInfo,
  showRubles = false,
  className,
  limitType,
  transportType,
  isDepartmentHead,
}) => {
  const { data: limitColorsPercentValues } = useGetLimitColorsPercentInfo();
  const { logger } = useAppStoreContext();

  const currentTypeColorsPercentValues = filterColorsValues(limitType, limitColorsPercentValues);

  const colorsPercent = R.mergeAll(
    currentTypeColorsPercentValues.map(element => ({
      [element.name as string]: element.value,
    }))
  );

  const colorsObjectSpecified: any = {};
  const shortObjectsKeys = (localValue: string, key: string): void => {
    // key is being shorted to be the same for personal and department limitType of limit -
    // the end of the string (beginning from 11 index) is the same for both of them
    //  for example EMP_LIMIT_ or DEP_LIMIT_
    // eslint-disable-next-line no-param-reassign
    key = key.substring('EMP_LIMIT_'.length, key.length);
    // FIXME no-param-reassign
    colorsObjectSpecified[key] = localValue;
  };

  R.forEachObjIndexed(shortObjectsKeys as any, colorsPercent);

  const color = chooseColorByPercent(percent, colorsObjectSpecified);

  return (
    <div className={className}>
      <Progress
        trailColor="#bfbfbf"
        strokeColor={{
          '0%': color,
          '100%': color,
        }}
        percent={percent ?? 0}
        status="normal"
        className={styles.progress}
        showInfo={showInfo}
        strokeLinecap="square"
      />
      {showInfo && (
        <div className={styles.controls}>
          <div>
            {name}
            {' '}
            {showRubles && (
              <span>
                <NumberFormat
                  value={value}
                  displayType="text"
                  thousandSeparator="&thinsp;"
                  suffix={` ${RUBLE_SIGN}`}
                />
              </span>
            )}
          </div>

          {((isDepartmentHead && limitType === LIMIT_TYPE.DEPARTMENT) || limitType === LIMIT_TYPE.EMPLOYEE) && (
            <LimitRequestModal
              percent={percent ?? 0}
              transportType={transportType ?? ''}
              type={limitType}
            />
          )}

          {!isDepartmentHead && limitType === LIMIT_TYPE.DEPARTMENT && percent < 15 && (
            <Button
              size="middle"
              onClick={(): void => {
                logger.toMessage('warning', 'Механизм push-уведомлений находится в разработке');
                // TODO реализовать
              }}
            >
              Уведомить
            </Button>
          )}
        </div>
      )}
    </div>
  );
};

export { checkCondition, filterColorsValues, chooseColorByPercent };

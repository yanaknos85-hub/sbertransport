import React, { Dispatch, FC, SetStateAction } from 'react';
import { LabeledValue } from 'antd/es/select';
import { FormInstance } from 'antd/lib/form';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { LIMIT_TYPE } from 'stores/Limits/Limit.interface';
import { SiblingsParams } from 'stores/Limits/LimitsRequest.interface';
import { StoreNames } from 'stores/StoreNames.enum';
import * as tt from 'utils/io-ts';
import { getYear } from 'utils/Misc';

import { ISpentActionsType } from '../useDetailedLimitsMapper';
import { LimitRequestLevel, monthArray, siblingsLimitMoneyPercent } from './constants';
import { LimitRequestFormContent } from './LimitRequestFormContent';
import { useLimitRequestForm } from './useLimitRequestForm';

interface LimitRequestFormProps {
  formRef: FormInstance;
  isEdit?: boolean;
  request?: ISpentActionsType;
  type: LIMIT_TYPE;
  defaultTransportType?: string;
  setTT?: Dispatch<SetStateAction<string>>;
}

export interface Fields {
  transportType: string;
  level: LimitRequestLevel;
  reason: string;
  sum: number;
  departments: tt.UUID[];
}

export const LimitRequestFormController: FC<LimitRequestFormProps> = ({
  formRef,
  isEdit = false,
  request,
  type,
  defaultTransportType,
  setTT,
}) => {
  // FIXME sonarjs/cognitive-complexity
  const { [StoreNames.limitsRequestStore]: limitsRequestStore, [StoreNames.selfStore]: selfStore }
    = useAppStoreContext();

  if (!isEdit) {
    formRef.setFieldsValue({ transportType: defaultTransportType });
  }

  const [depSiblings, setDepSiblings] = React.useState<LabeledValue[]>([]);

  const resetDepartments = (options: any[]): void => {
    /**
     * 1. пустой массив
     * 2. выбранный набор и текущий список доступных отличаются. Оставить только совпадающие элементы
     */

    if (!options.length) {
      formRef.resetFields(['departments']);
    } else {
      const { departments } = formRef.getFieldsValue();
      const deps: string[] = [];
      if (departments) {
        departments.forEach((x: string) => {
          if (options.some(y => y.value === x)) {
            deps.push(x);
          }
        });

        formRef.setFieldsValue({ departments: deps });
      }
    }
  };

  const setDepType = (e: LimitRequestLevel): void => {
    if (e === LimitRequestLevel.SIBLINGS) {
      const {
        period, sum, transportType,
      } = formRef.getFieldsValue();
      if (period && sum && transportType) {
        const data = {
          departmentId: selfStore.depId,
          percent: siblingsLimitMoneyPercent,
          transportType,
          year: getYear(period),
          sum,
        };
        limitsRequestStore.getDepSiblings(data as SiblingsParams).then(siblings => {
          const options = siblings.map(department => ({
            value: department.id,
            label: department.departmentName,
          }));
          setDepSiblings(options);
          resetDepartments(options);
        });
      }
    }
  };

  const updateSiblings = (): void => {
    if (formRef.getFieldValue('level') === LimitRequestLevel.SIBLINGS) {
      setDepType(LimitRequestLevel.SIBLINGS);
    }
  };

  const { initialRequestValues } = useLimitRequestForm(isEdit, setDepSiblings, formRef, request, monthArray);

  return (
    <LimitRequestFormContent
      limitType={type}
      isEdit={isEdit}
      formRef={formRef}
      initialRequestValues={initialRequestValues}
      depSiblings={depSiblings}
      updateSiblings={updateSiblings}
      setTT={setTT}
    />
  );
};

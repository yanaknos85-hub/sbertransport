/* eslint-disable @typescript-eslint/no-explicit-any */
import { SelectValue } from 'antd/lib/select';
import React, { Fragment, PropsWithChildren } from 'react';
import { useTranslation } from 'i18n';
import { SelectDepartment } from 'shared/components/SelectDepartment';
import { ignore } from 'utils';
import { UUID } from 'utils/io-ts';

import styles from './styles.module.scss';

export interface SelectDepartmentProps {
  value: UUID[][];
  options?: {
    value: UUID;
    label: string;
  }[];
  index: number;
  handleSelect: (n: number) => (ids: UUID[]) => void;
}

const DefaultContainer = ({ children }: { children: any }) => <div className={styles.selectItemsBlock}>{children}</div>;

const DefaultContainerItem = ({ children, index = 0 }: PropsWithChildren<{ index?: number }>) => (
  <Fragment key={index}>{children}</Fragment>
);

export const SelectDepartments: React.FC<{
  value?: UUID[][];
  onChange?: (xs: UUID[][]) => void;
  Container?: ({ children }: { children: any }) => JSX.Element;
  ContainerItem?: ({ children }: { children: any }) => JSX.Element;
  propsByIndex?: (index: number) => Record<string, unknown>;
  orgId?: UUID;
}> = ({
  propsByIndex,
  value = [[], [], [], [], [], []],
  onChange = ignore,
  Container = DefaultContainer,
  ContainerItem = DefaultContainerItem,
  orgId,
}) => {
  const { t } = useTranslation();

  const handleSelect = (n: number) => (ids: SelectValue) => {
    const newValue = value.map((valueInCell, i) => {
      if (n === i) {
        return ids as UUID[];
      }
      return valueInCell;
    });
    onChange(newValue);
  };

  return (
    <Container>
      {value?.map((_, i) => (
        <ContainerItem key={i}>
          <SelectDepartment
            {...propsByIndex?.(i)}
            placeholder={`${t.DepartmentLevels.department} ${i + 1} ${t.DepartmentLevels.level}`}
            // @ts-ignore
            onChange={handleSelect(i)}
            mode="multiple"
            orgId={orgId}
          />
        </ContainerItem>
      ))}
    </Container>
  );
};

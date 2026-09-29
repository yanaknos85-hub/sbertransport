import React, { FC, Suspense, useState } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { useTranslation } from 'i18n';
import { ColumnProps } from 'antd/lib/table';
import { PlusOutlined } from '@ant-design/icons';

import { toWildcardRegexp } from 'utils';

import { CargoType } from 'stores/CargoType/CargoType.interface';

import { useDeleteCargoType, useCargoTypes } from 'api/cargo-types';
import { columnsPropsFactory } from 'shared/columnsPropsFactory';
import { TableEditButtons } from 'shared/components/TableEditButtons.tsx/TableEditButtons';
import { ModelFormFieldProps } from 'shared/models/ModelDetail/ModelFormField';
import { FilterPanel } from 'shared/components/FilterPanel';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { TableShadow } from 'shared/components/TableShadow';
import { Button } from 'shared/components/Button/Button';
import { useModalState } from 'shared/hooks/useModal';
import { Modal } from 'shared/components/Modal/Modal';
import { useColumns, CargoTypeRecord } from './Components/Columns';
import { useFields } from './Components/Fields';
import { CargoTypeDetailed } from './Components/CargoTypeDetailed';
import styles from './CargoType.module.scss';
import { useOrganizationContext } from '../../context/Organization.context';

type FilterValues = Partial<Record<keyof CargoType, any>>;

const useFilterFields: () => ModelFormFieldProps[] = () => useFields()
  .filter(field => ['name', 'type'].includes(field.name))
  .map(field => ({
    ...field,
    label: field.description,
    required: false,
  }));

const isMatching = (cargoType: CargoType, filters: FilterValues) => !Object.keys(filters).some(filterName => {
  const filterValue = filters[filterName as keyof CargoType];
  if (filterValue === undefined || filterValue.length === 0) {
    return false;
  }

  const value: unknown = cargoType[filterName as keyof CargoType];
  if (Array.isArray(filterValue)) {
    if (Array.isArray(value)) {
      return !value.some(v => filterValue.includes(v));
    }
    return !filterValue.includes(value);
  }
  if (typeof filterValue === 'string') {
    const regexp = toWildcardRegexp(filterValue);
    return !regexp.test(value as string);
  }
  return filterValue !== value;
});

export const CargoTypeHandbookComponent: FC = () => {
  const [filters, setFilters] = useState<FilterValues>({});
  const fields = useFilterFields();

  return (
    <Suspense fallback={<SpinWrapped />}>
      <FilterPanel
        className={styles.filtersWrapper}
        onApplyFilters={setFilters}
        fields={fields}
        isStatusChangeActive
      />
      <TheTable filters={filters} />
    </Suspense>
  );
};

const TheTable: FC<{ filters: FilterValues }> = ({ filters }) => {
  const { t } = useTranslation();
  const [cargoTypeId, setCargoTypeId] = React.useState<string>('adding');
  const { organizationId } = useOrganizationContext();

  const [visible, { hide, show }] = useModalState();
  const { data } = useCargoTypes(organizationId);

  const [deletePosition] = useDeleteCargoType();
  const match = useRouteMatch();

  const cargoTypesFiltered = React.useMemo(() =>  (
    data.cargoTypes?.filter(cargoType => isMatching(cargoType, filters))
    ), [
    data.cargoTypes,
    filters,
  ]);

  const handleEdit = (id: string): void => {
    setCargoTypeId(id);
    show();
  };

  const handleClose = (): void => {
    setCargoTypeId('');
    hide();
  };

  const handleAddNewClick = (): void => {
    setCargoTypeId('adding');
    show();
  };

  const editButtons: ColumnProps<CargoTypeRecord> = {
    fixed: 'right',
    width: 60,
    render: (_: any, record: CargoTypeRecord): JSX.Element => (
      <TableEditButtons
        onEdit={handleEdit}
        path={match.path}
        id={record.id}
        onDelete={() => deletePosition({ cargoTypeId: record.id, organizationId })}
        title={t.global.deleteConfirm}
      />
    ),
  };

  const columnsProps = columnsPropsFactory<CargoTypeRecord>([...useColumns(), editButtons]);

  return (
    <>
      <TableShadow
        columns={columnsProps}
        dataSource={cargoTypesFiltered}
        scroll={{ x: 600 }}
        rowKey="id"
        tableLayout="auto"
        footer={() => <Footer handleAddNewClick={handleAddNewClick} />}
        className={styles.cargoTypeTable}
      />
      <Modal
        onCancel={handleClose}
        visible={visible}
        footer={null}
        destroyOnClose
      >
        <CargoTypeDetailed
          cargoTypeId={cargoTypeId}
          hide={hide}
        />
      </Modal>
    </>
  );
};

const Footer = React.memo(({ handleAddNewClick }: { handleAddNewClick: () => void }) => {
  const { t } = useTranslation();

  return (
    <div className={styles.addButtonContainer}>
      <Button
        icon={<PlusOutlined />}
        size="small"
        onClick={handleAddNewClick}
        className={styles.addButton}
      >
        {t.global.add}
      </Button>
    </div>
  );
});

export default CargoTypeHandbookComponent;

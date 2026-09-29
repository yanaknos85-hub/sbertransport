import React, { FC, Suspense, useState } from 'react';
import { useHistory, useRouteMatch } from 'react-router-dom';
import { useTranslation } from 'i18n';
import { Button, Table } from 'antd';
import { ColumnProps } from 'antd/lib/table';
import { PlusOutlined } from '@ant-design/icons';

import { useProfile } from 'api/profile';
import { useDeletePosition, usePositions, useUploadPositions } from 'api/positions';
import { Position } from 'stores/Position/Position.interface';
import { columnsPropsFactory } from 'shared/columnsPropsFactory';
import { TableEditButtons } from 'shared/components/TableEditButtons.tsx/TableEditButtons';
import { toWildcardRegexp } from 'utils';
import { UUID } from 'utils/io-ts';

import { ModelFormFieldProps } from 'shared/models/ModelDetail/ModelFormField';
import { FilterPanel } from 'shared/components/FilterPanel';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import DownloadButton from 'components/DownloadButton';
import { StructureLoadAccess, useDownloadParams } from 'components/Structure/StructureLoadAccess';
import { useColumns, PositionRecord } from './Components/Columns';

import { useFields } from './Components/Fields';

import styles from './positions.module.scss';
import { UploadButton, importExportEndpointMap } from '../UploadButton';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type FilterValues = Partial<Record<keyof Position, any>>;

const useFilterFields: () => ModelFormFieldProps[] = () => useFields(true)
  .filter(field => field.editable)
  .map(field => ({
    ...field,
    label: field.description,
    required: false,
  }));

const isMatching = (position: Position, filters: FilterValues) => !Object.keys(filters).some(filterName => {
  const filterValue = filters[filterName as keyof Position];
  if (filterValue === undefined || filterValue.length === 0) {
    return false;
  }

  const value: unknown = position[filterName as keyof Position];
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

const Footer = React.memo(() => {
  const { t } = useTranslation();
  const history = useHistory();
  const match = useRouteMatch();

  const handleAddNewClick = (): void => {
    history.push(`${match.path}/adding`);
  };

  return (
    <div className="table_footer">
      <Button
        icon={<PlusOutlined />}
        size="middle"
        onClick={handleAddNewClick}
      >
        {t.global.add}
      </Button>
    </div>
  );
});

const PositionTable: FC<{ filters: FilterValues }> = ({ filters }) => {
  const { t } = useTranslation();

  const { organizationId: myOrgId } = useProfile().data;
  const orgId: UUID = filters.organizationId || myOrgId;
  const { positions } = usePositions(orgId).data;

  const positionsFiltered = React.useMemo(() => positions.filter(position => isMatching(position, filters)), [
    positions,
    filters,
  ]);

  const [deletePosition] = useDeletePosition();
  const match = useRouteMatch();
  const editButtons: ColumnProps<PositionRecord> = {
    fixed: 'right',
    width: 60,
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    render: (_: any, record: PositionRecord): JSX.Element => (
      <TableEditButtons
        path={match.path}
        id={record.id}
        onDelete={record.active ? () => deletePosition({ orgId, posId: record.id }) : undefined}
        title={t.global.deleteConfirm}
      />
    ),
  };

  const columnsProps = columnsPropsFactory<PositionRecord>([...useColumns(), editButtons]);

  return (
    <Table
      columns={columnsProps}
      dataSource={positionsFiltered}
      bordered
      scroll={{ x: 600 }}
      rowKey="id"
      size="small"
      tableLayout="auto"
      footer={() => <Footer />}
      className={styles.table}
    />
  );
};

const PositionsHandbookComponent: FC = () => {
  const [filters, setFilters] = useState<FilterValues>({});
  const downloadParams = useDownloadParams();
  const filterFields = useFilterFields();

  return (
    <>
      <StructureLoadAccess>
        <UploadButton entity="position" useUpload={useUploadPositions} />
        <DownloadButton url={`${importExportEndpointMap.position}/files/position`} params={downloadParams} />
      </StructureLoadAccess>
      <FilterPanel
        onApplyFilters={setFilters}
        fields={filterFields}
        isStatusChangeActive
      />
      <Suspense fallback={<SpinWrapped />}>
        <PositionTable filters={filters} />
      </Suspense>
    </>
  );
};

export default PositionsHandbookComponent;

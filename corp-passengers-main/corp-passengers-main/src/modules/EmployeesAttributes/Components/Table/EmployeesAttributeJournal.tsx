import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import React, { FC, useEffect, useMemo } from 'react';
import { Button } from 'antd';
import { observer } from 'mobx-react';
import {
  useCreateEmployeeAttribute,
  useDeleteEmployeeAttribute,
  useUpdateEmployeeAttribute,
  useUploadEmployeeAttributes
} from 'api/employee-attributes';
import { useTranslation } from 'i18n';
import { EditableColumnType, EditableTable, EditableTableStore } from 'shared/components/EditableTable';
import createKeyGen from 'utils/keygen';
import { UUID } from 'utils/io-ts';
import { useProfile } from 'api/profile';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import DownloadButton from 'components/DownloadButton';
import { StructureLoadAccess } from 'components/Structure/StructureLoadAccess';
import { UploadButton, importExportEndpointMap } from 'modules/UploadButton';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { ToolbarProvider } from 'components/Toolbar';
import { PanelTitle } from 'components/Panel';
import { TextField } from '../TextField/TextField';
import style from './EmployeeAttributes.module.scss';

const Item = t.type({
  targetParentLimit: t.string,
  targetEmployeeId: tt.uuid,
  transportType: t.string,
  sum: tt.money,
});
export type Item = t.TypeOf<typeof Item>;

type RecordEmployee = Omit<Item, 'targetEmployeeId'> & {
  id: string;
  isNotSaved: boolean;
  employee?: string;
  targetEmployeeId?: UUID;
  limitStatus: string;
  year: number;
  index: number;
  of: number;
};

export interface AttributeRecord {
  id: string;
  isNew: boolean;
  name: string;
}

const keyGen = createKeyGen();

const EmployeesAttributeJournal: FC = () => {
  const { t } = useTranslation();
  const profile = useProfile().data;
  const { logger } = useAppStoreContext();
  // @ts-ignore
  const attributes = useActiveEmployeeAttributes(profile.organizationId).data;
  const [deleteAttribute, { isLoading: deleting }] = useDeleteEmployeeAttribute();
  const [updateAttribute, { isLoading: updating }] = useUpdateEmployeeAttribute();
  const [create, { isLoading: creating }] = useCreateEmployeeAttribute();

  const store = useMemo(
    () => new EditableTableStore<AttributeRecord>({
      rowKeyFn: (record: AttributeRecord) => record.id,
    }),
    []
  );

  const addRow = () => {
    const item: AttributeRecord = {
      id: keyGen.next().value,
      name: '',
      isNew: true,
    };
    store.data.unshift(item);
    store.startEditing(item.id);
  };

  useEffect(() => {
    store.handleSave = async ({
      name, id, isNew,
    }: AttributeRecord) => {
      if (!name) {
        logger.toMessage('error', 'Поле Название не может быть пустым!');
        return Promise.reject();
      }

      const result = isNew
        ? await create({ attribute: { name } })
        : await updateAttribute({ attribute: { id: id as UUID, name }, eAttrId: id as UUID });
      return !!result;
    };
  }, [store, create, logger, updateAttribute]);

  useEffect(() => {
    store.handleDelete = async ({ id }: AttributeRecord) => {
      const result = await deleteAttribute({ eAttrId: id as UUID });
      return !!result;
    };
  }, [store, deleteAttribute]);

  store.data = attributes.map(attr => ({
    id: attr.id, name: attr.name, isNew: false,
  }));

  const useColumns = (): EditableColumnType<RecordEmployee>[] => useMemo(
    (): EditableColumnType<RecordEmployee>[] => [
      {
        title: 'Название',
        dataIndex: 'name',
        key: 'name',
        width: 50,
        render: props => <TextField {...props} />,
      },
    ],
    []
  );

  return (
    <div className={style.main_table_wrapper}>
      <Button
        className={style.createButton}
        type="primary"
        onClick={addRow}
      >
        {t.EmployeeAttributes.createAttribute}
      </Button>
      <StructureLoadAccess>
        <UploadButton entity="employeeAttributes" useUpload={useUploadEmployeeAttributes} />
        <DownloadButton url={`${importExportEndpointMap.employeeAttributes}/files/employeeAttributes`} />
      </StructureLoadAccess>
      <EditableTable
        store={store}
        columns={useColumns()}
        bordered
        style={{ overflow: 'scroll' }}
        size="small"
        tableLayout="auto"
      />
      {(deleting || updating || creating) && <SpinWrapped />}
    </div>
  );
};

const withProvider = observer(() => (
  <ToolbarProvider dataName="passenger_dir_attributes">
    <PanelTitle>Признаки сотрудника</PanelTitle>
    <EmployeesAttributeJournal />
  </ToolbarProvider>
));

export default withProvider;

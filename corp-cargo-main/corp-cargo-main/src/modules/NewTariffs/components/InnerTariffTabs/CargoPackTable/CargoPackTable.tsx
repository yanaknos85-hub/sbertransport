import React, { useState, useRef, useContext, useEffect, FC } from 'react';
import Form, { FormInstance } from 'antd/lib/form';
import { Table, Popconfirm, Button, Select, InputNumber } from 'antd';
import { DeleteOutlined } from '@ant-design/icons';
import { convertToRubles } from 'utils';
import classNames from 'classnames';
import {
  useGetPackData,
  useGetPackTariff,
  usePostPackTariff,
  usePutPackTariff
} from 'api/tariffs-cargo';
import { FieldType } from 'shared/form/Field/Field';
import { CargoPackPost, PackDetails } from 'stores/Tariffs/Tariffs.interface';
import styles from './CargoPackTable.module.scss';

const EditableContext = React.createContext<FormInstance<any> | null>(null);

type EditableRowProps = {
  index: number;
};

type Item = {
  pack: string;
  cost: string;
};

type EditableCellProps = {
  title: React.ReactNode;
  editable: boolean;
  children: React.ReactNode;
  dataIndex: keyof Item;
  record: Item;
  handleSave: (record: Item) => void;
  inputType: string;
};

export const packTariffFields = {
  pack: {
    label: 'Упаковочный материал',
    name: 'pack',
    type: FieldType.select,
  },
  cost: {
    label: 'Стоимость',
    name: 'cost',
    type: FieldType.number,
  },
};
// todo Необходима доработка и рефакторинг
const EditableCell: React.FC<EditableCellProps> = ({
  title,
  editable,
  children,
  dataIndex,
  record,
  handleSave,
  inputType,
  ...restProps
}) => {
  const [editing, setEditing] = useState(false);
  const inputRef = useRef<any>(null);
  const inputSelect = useRef<any>(null);
  const form = useContext(EditableContext);

  const { content: packs } = useGetPackData().data;

  const packOptions: { label: string; value: string }[] = packs?.map(({ name }) => ({
    label: name,
    value: name,
  }));

  useEffect(() => {
    if (editing) {
      inputRef.current?.focus();
    }
  }, [editing]);

  const toggleEdit = () => {
    setEditing(!editing);
    form?.setFieldsValue({ [dataIndex]: record[dataIndex] });
  };

  const save = async () => {
    try {
      const values = await form?.validateFields();
      toggleEdit();
      handleSave({ ...record, ...values });
    } catch (errInfo) {
      console.log('Save failed:', errInfo);
    }
  };

  const inputNode =
    inputType === 'number' ? (
      <InputNumber ref={inputRef} min={0} onPressEnter={save} onBlur={save} {...packTariffFields.cost} />
    ) : (
      <Select
        ref={inputSelect}
        allowClear
        showSearch
        placeholder="Введите наименование"
        onBlur={save}
        {...packTariffFields.pack}
        options={packOptions}
      />
    );

  let childNode = children;

  if (editable) {
    childNode = editing ? (
      <Form.Item
        style={{ margin: 0 }}
        name={dataIndex}
        rules={[
          {
            required: true,
            message: `${title} обязательно для заполнения.`,
          },
        ]}
      >
        {inputNode}
      </Form.Item>
    ) : (
      <div className="editable-cell-value-wrap" style={{ paddingRight: 24 }} onClick={toggleEdit}>
        {children}
      </div>
    );
  }

  return <td {...restProps}>{childNode}</td>;
};

const EditableRow: React.FC<EditableRowProps> = ({ index, ...props }) => {
  const [form] = Form.useForm();
  return (
    <Form form={form} component={false}>
      <EditableContext.Provider value={form}>
        <tr {...props} />
      </EditableContext.Provider>
    </Form>
  );
};

type DataType = {
  key: React.Key;
  pack: string;
  cost: number;
  editable: boolean;
  packDetails?: PackDetails[];
};

type CargoPackTableProps = {
  contractId?: string;
  contractorId?: string;
};

export const CargoPackTable: FC<CargoPackTableProps> = props => {
  const { contractId, contractorId } = props;

  const { data } = useGetPackTariff(contractId);
  const [count, setCount] = useState(0);

  const [postPackTariff] = usePostPackTariff();
  const [putPackTariff] = usePutPackTariff();

  const [tableSource, setTableSource] = useState<any[] | undefined>(data.packDetails);
  const { content: packsList } = useGetPackData().data;

  useEffect(() => {
    const res = data.packDetails
      ? data.packDetails?.reduce((acc: {id: string, key: number; pack: string; cost: number }[], { pack, cost }: PackDetails, idx: number) => {
          acc.push({ id: pack.id, key: idx + 1, pack: pack.name, cost: convertToRubles(cost) });
          return acc;
        }, [])
      : [];
    setCount(res.length + 1);
    setTableSource(res);
  }, []);

  const handleDelete = (key: React.Key) => {
    const newData = tableSource?.filter(item => {
      return item.key !== key;
    });
    // @ts-ignore
    setTableSource(() => [...newData]);
  };

  const handleAdd = () => {
    const newData: DataType = {
      key: count,
      pack: 'Выберите название упаковки из списка',
      cost: 0,
      editable: true,
    };
    setTableSource(() => {
    // @ts-ignore
      return [...tableSource, newData]
    });
    setCount(count + 1)
  };

  const handleSave = (row: DataType) => {
    // @ts-ignore
    const newData = [...tableSource];
    const index = newData.findIndex(item => row.key === item.key);
    const item = newData[index];
    newData.splice(index, 1, {
      ...item,
      ...row,
    });
    setTableSource(newData);
  };

  const handlePostRequest = () => {
    const requestPackDetails = tableSource?.reduce((acc, cur) => {
      const temp = { pack : { ...packsList.find(pack => pack.name === cur.pack) }, cost: cur.cost * 100 };
      acc.push(temp);
      return acc;
    }, []);

    if (!data.empty) {
      const editTariffRequest = {
        ...data,
        packDetails: requestPackDetails,
      };
      putPackTariff(editTariffRequest)
    } else {
      const createTariffRequest = {
        contractorId,
        contractId,
        active: true,
        packDetails: requestPackDetails
      };
      postPackTariff(createTariffRequest as CargoPackPost);
    }
  };

  const components = {
    body: {
      row: EditableRow,
      cell: EditableCell,
    },
  };

  const useColumns = () => [
    { title: 'Название упаковки', dataIndex: 'pack', width: 300, editable: true },
    { title: 'Цена', dataIndex: 'cost', width: 100, editable: true },
    {
      title: 'Удалить',
      dataIndex: 'delete',
      width: 10,
      render: (_: any, record: { key: React.Key }): JSX.Element => (
        <div className={classNames(styles.tableEditButtons)}>
          <Popconfirm
            title="Хотите удалить?"
            onConfirm={() => {
              handleDelete(record.key);
            }}
          >
            <DeleteOutlined />
          </Popconfirm>
        </div>
      ),
    },
  ];

  const columns = useColumns().map(col => {
    if (!col.editable) {
      return col;
    }
    return {
      ...col,
      onCell: (record: DataType) => ({
        record,
        editable: col.editable,
        dataIndex: col.dataIndex,
        title: col.title,
        inputType: col.dataIndex === 'cost' ? 'number' : 'select',
        handleSave,
      }),
    };
  });

  return (
    <div>
      <Table
        components={components}
        rowClassName={() => 'editable-row'}
        bordered
        dataSource={tableSource}
        columns={columns}
        className={styles.table}
      />
      <Button onClick={handleAdd} type="primary" style={{ marginBottom: 16 }}>
        Добавить упаковку
      </Button>
      <Button onClick={handlePostRequest} type="primary" style={{ marginBottom: 16, marginLeft: 16 }}>
        Сохранить список
      </Button>
    </div>
  );
};

export default CargoPackTable;

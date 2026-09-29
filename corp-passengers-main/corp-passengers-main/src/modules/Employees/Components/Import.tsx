import * as React from 'react';
import {
  Button, Modal, Select, Checkbox, Form
} from 'antd';
import { useProfile } from 'api/profile';
import { useUploadLimits, LimitsUploadReport } from 'api/limits';
import styled from 'styled-components';
import { SelectFile } from '../../UploadButton';
import { UploadReport } from './UploadReport';

type CSVSeparator = 'COMMA' | 'SEMICOLON' | 'WHITESPACE' | 'TAB' | 'OTHER';
type FPSeparator = 'COMMA' | 'DOT';

export interface CSVParams {
  csvSeparator: CSVSeparator;
  fpSeparator: FPSeparator;
  includeHeader: boolean;
}

const separatorOptions = ([
  ['COMMA', 'Запятая'],
  ['SEMICOLON', 'Точка с запятой'],
  ['WHITESPACE', 'Пробел'],
  ['TAB', 'Табуляция'],
  ['OTHER', 'Другой'],
] as [CSVSeparator, string][]).map(([value, label]) => ({
  label,
  value,
}));

const fpSeparatorOptions = ([
  ['COMMA', 'Запятая'],
  ['DOT', 'Точка'],
] as [FPSeparator, string][]).map(([value, label]) => ({
  label,
  value,
}));

const StyledCSVOptions = styled.div`
  display: flex;
  flex-direction: column;
  margin-top: 16px;
`;

const CSVOptions: React.FC<{
  values: Partial<CSVParams>;
  onChange: (values: Partial<CSVParams>) => void;
  csv?: boolean;
}> = ({ onChange, csv = false }) => {
  const handleChange = React.useCallback(
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (_: any, vs: Partial<CSVParams>) => {
      onChange(vs);
    },
    [onChange]
  );

  return (
    <StyledCSVOptions>
      <Form
        initialValues={{
          includeHeader: false,
          csvSeparator: 'SEMICOLON',
          fpSeparator: 'COMMA',
        }}
        onValuesChange={handleChange}
      >
        <Form.Item name="includeHeader" valuePropName="checked">
          <Checkbox>С заголовком</Checkbox>
        </Form.Item>
        {csv ? (
          <>
            <Form.Item name="csvSeparator" label="Разделитель записей">
              <Select options={separatorOptions} />
            </Form.Item>
            <Form.Item name="fpSeparator" label="Десятичный разделитель">
              <Select options={fpSeparatorOptions} />
            </Form.Item>
          </>
        ) : null}
      </Form>
    </StyledCSVOptions>
  );
};

const isCSV = (file: File): file is File & { type: 'text/csv' } => file.type === 'text/csv';

const ImportEmployee: React.FC = () => {
  const { organizationId } = useProfile().data;

  const [modalVisible, setModalVisible] = React.useState(false);

  const showModal = React.useMemo(() => () => setModalVisible(true), [setModalVisible]);

  const hideModal = React.useMemo(() => () => setModalVisible(false), [setModalVisible]);

  const [file, setFile] = React.useState<File | null>(null);

  const [busy, setBusy] = React.useState(false);

  const [uploadResult, setUploadResult] = React.useState<LimitsUploadReport | null>(null);

  const [uploadError, setUploadError] = React.useState<Error | null>(null);

  // @ts-ignore
  const [upload] = useUploadLimits({ organizationId });

  const [csvParams, setCsvParams] = React.useState<Partial<CSVParams>>({});

  const [simulate, setSimulate] = React.useState(false);

  const handleUpload = React.useMemo(
    () => () => {
      setBusy(true);

      upload({
        file: file!, simulate, updateRule: 'ADD', ...csvParams,
      })
        .then(result => {
          setUploadResult(result!);
        })
        .catch(err => setUploadError(err))
        .finally(() => setBusy(false));
    },
    [file, upload, csvParams, simulate]
  );

  const footer = React.useMemo(
    () => uploadResult || uploadError
      ? [
        <Button key="ok" onClick={hideModal}>
          OK
        </Button>,
      ]
      : [
        <Checkbox checked={simulate} onChange={({ target: { checked } }) => setSimulate(checked)}>
          Симуляция импорта
        </Checkbox>,
        <Button
          key="cancel"
          onClick={hideModal}
          disabled={busy}
        >
          Отмена
        </Button>,
        <Button
          key="upload"
          disabled={!file || busy}
          onClick={handleUpload}
          loading={busy}
        >
          Загрузить
        </Button>,
      ],
    [busy, file, handleUpload, uploadResult, uploadError, hideModal, simulate]
  );

  React.useEffect(() => {
    setFile(null);
    setUploadResult(null);
    setUploadError(null);
    setCsvParams({});
    setSimulate(false);
  }, [modalVisible]);

  return (
    <>
      <Button onClick={showModal}>Импорт</Button>

      <Modal
        title="Импорт сотрудников"
        visible={modalVisible}
        onCancel={hideModal}
        footer={footer}
        destroyOnClose
      >
        {uploadError ? (
          <div>Ошибка импорта лимитов</div>
        ) : uploadResult ? (
          <UploadReport uploadResult={uploadResult} />
        ) : (
          <>
            <SelectFile {...{
              file, setFile, accept: '.csv,.xlsx',
            }}
            />

            {file ? (
              <CSVOptions
                csv={isCSV(file)}
                values={csvParams}
                onChange={setCsvParams}
              />
            ) : null}
          </>
        )}
      </Modal>
    </>
  );
};

export { ImportEmployee };

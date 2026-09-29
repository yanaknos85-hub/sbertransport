import { Button, Modal } from 'antd';
import * as React from 'react';
import { MutationResultPair } from 'react-query';
import styled from 'styled-components';
import { Link } from 'react-router-dom';
import { ImportReportSummary } from '../ImportReport';
import { MAIN_PLATFORM } from 'constants/constants.routes';

export enum Modes {
  Passengers = 'passengers',
  Cargo = 'cargo',
}

const FileSelector = styled.div`
  display: flex;
  flex-direction: row;
  align-items: center;
`;

const FileName = styled.span`
  margin-left: 16px;
`;

const Message = styled.div``;

const Error = styled(Message)`
  color: red;
`;

const Success = styled(Message)``;

export const importExportEndpointMap = {
  tariff: '/tariffs',
  tariffCargo: '/tariff-cargo',
  zones: '/geo-zones',
  employee: '/organizations',
  department: '/organizations',
  position: '/organizations',
  tripPurpose: '/organizations',
  organization: '/organizations',
  employeeAttributes: '/organizations',
  location: '/organizations',
  meetingAddress: '/organizations',
  contractor: '/contractors',
  contract: '/tariffs',
  contractCargo: '/tariff-cargo',
  courier: '/tariff-cargo',
  interregional: '/tariff-cargo',
  domestic_courier: '/tariff-cargo',
  workingGroups: '/tariffs',
} as const;

export const SelectFile: React.FC<{
  file: File | null;
  setFile: (file: File) => void;
  busy?: boolean;
  accept?: React.InputHTMLAttributes<File>['accept'];
}> = ({
  file, setFile, busy = false, accept = '.xls,.xlsx, .pdf',
}) => {
  const input = React.useRef<HTMLInputElement>(null);

  const handleSelect = () => {
    if (input.current) {
      input.current.click();
    }
  };

  const handleChange: React.ChangeEventHandler<HTMLInputElement> = evt => {
    const selectedFile = (evt.target as HTMLInputElement & { files: FileList }).files[0];

    if (selectedFile) {
      setFile(selectedFile);
    }
  };

  return (
    <FileSelector>
      <Button onClick={handleSelect} disabled={busy}>
        Выбрать файл
      </Button>
      <input
        ref={input}
        hidden
        type="file"
        accept={accept}
        onChange={handleChange}
      />
      {file ? <FileName>{file.name}</FileName> : null}
    </FileSelector>
  );
};

export const UploadButton: React.FC<{
  title?: string;
  entity: string;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  useUpload: () => MutationResultPair<any, unknown, { file: File }, unknown>;
  customElement?: JSX.Element;
  mode?: Modes;
  onSuccessReport?: () => void;
}> = ({
  title = 'Импорт', useUpload, entity, customElement, mode, onSuccessReport,
}) => {
  const [modalVisible, setModalVisible] = React.useState(false);
  const [file, setFile] = React.useState<File | null>(null);
  const [uploadFinished, setUploadFinished] = React.useState(false);
  const [busy, setBusy] = React.useState(false);
  const [error, setError] = React.useState<Error | null>(null);

  const showModal = () => setModalVisible(true);
  const hideModal = () => setModalVisible(false);

  const [upload] = useUpload();

  const handleUpload = React.useCallback(() => {
    if (!file) {
      return;
    }
    if (file.name.endsWith('.xlsx') && mode === Modes.Cargo) {
      Modal.error({
        title: 'Ошибка',
        content: 'В данный момент загрузка файлов с расширением .xlsx не поддерживается',
      });
      return;
    }

    setBusy(true);

    upload({ file })
      .then(() => {
        setError(null);
      })
      .catch(err => {
        setError(err);
        setBusy(false);
      })
      .finally(() => {
        setUploadFinished(true);
      });
  }, [file, upload, mode]);

  const footer = React.useMemo(
    () => [
      uploadFinished && !error ? (
        <>
          <Button key="ok" onClick={hideModal}>
            OK
          </Button>
          <Button
            key="show"
            onClick={hideModal}
            loading={busy}
          >
            <Link to={`${MAIN_PLATFORM}/import-report/${entity}`}>Просмотреть результаты</Link>
          </Button>
        </>
      ) : (
        <>
          <Button
            key="cancel"
            onClick={hideModal}
            disabled={busy}
          >
            Отмена
          </Button>
          <Button
            key="upload"
            disabled={!file || busy}
            onClick={handleUpload}
            loading={busy}
          >
            Загрузить
          </Button>
        </>
      ),
    ],
    [busy, entity, error, file, handleUpload, uploadFinished]
  );

  const button = customElement ? (
    React.cloneElement(customElement, { onClick: showModal, ['data-name']: 'passenger_dir_import' })
  ) : (
    <Button onClick={showModal} data-name="passenger_dir_import">
      {title}
    </Button>
  );

  return (
    <>
      {button}
      <Modal
        title="Загрузка"
        visible={modalVisible}
        footer={footer}
        onCancel={hideModal}
      >
        {uploadFinished ? (
          error ? (
            <>
              <Error>
                Ошибка импорта:
                {error.toString()}
              </Error>
              <SelectFile {...{
                file, setFile, busy,
              }}
              />
            </>
          ) : (
            <Success>
              <ImportReportSummary
                entity={entity as keyof typeof importExportEndpointMap}
                setBusy={setBusy}
                onSuccessReport={onSuccessReport}
              />
            </Success>
          )
        ) : (
          <SelectFile {...{
            file, setFile, busy,
          }}
          />
        )}
      </Modal>
    </>
  );
};

import React, { FC, useEffect, useState } from 'react';
import { Modal } from 'antd';
import { useHistory } from '@sber-sbertransport/mf-core';

import { useTranslation } from 'i18n';
import { MULTI_LOGISTICS_ETRN_TAB } from 'constants/constants.routes';
import { signFileByCertificate } from 'modules/Planner/Components/EtrnSignature/utils/cryptoPro';
import { useCryptoPro } from 'modules/Planner/Components/EtrnSignature/hooks/useCryptoPro';
import { Messages } from 'modules/Planner/Components/EtrnSignature/constants/CryptoPro';
import { useSendEtrn } from 'api/etrn-signature/etrn-signature';
import { EtrnTitle } from 'modules/Planner/Components/EtrnSignature/types';

import Loading from './Loading/Loading';
import Error from './Error/Error';
import Certificates from './Certificates/Certificates';
import { TitleType } from '../../constants';
import { b64DecodeUnicode } from 'utils/Misc';

interface Props {
  visible: boolean;
  cardId: string;
  /**
   * Объект титула T3, полученный через `useGetEtrnTitle`.
   * Используется для подписания (`title.content`, декодированный для КриптоПро)
   * и для отправки результата на бэкенд (`title.fileName`, `title.creationTime`).
   * В POST `file` отправляется как исходный base64.
   */
  title: EtrnTitle;
  onClose: () => void;
  onSigned: () => void;
}

const SignT3Modal: FC<Props> = ({
  visible,
  cardId,
  title,
  onClose,
  onSigned,
}) => {
  const history = useHistory();
  const { messages, certificates, addMessage } = useCryptoPro();
  const [sendEtrn] = useSendEtrn();
  const { signModal: i18 } = useTranslation().t.Etrn;

  const [thumbprint, setThumbprint] = useState<string | null>(null);
  const [isSigning, setIsSigning] = useState(false);

  // Сбрасываем выбранный сертификат при каждом новом открытии модалки,
  // чтобы не подписывать старым выбором.
  useEffect(() => {
    if (visible) setThumbprint(null);
  }, [visible]);

  const isLoading = messages === null || isSigning;
  const hasError = !isLoading && messages !== null && messages.length > 0;

  const modalProps = isLoading || hasError
    ? { title: null, footer: null }
    : { title: i18.title };

  const handleOk = () => {
    if (thumbprint && cardId) {
      setIsSigning(true);

      signFileByCertificate(thumbprint, b64DecodeUnicode(title.content))
        .then((signature: string) => {
          sendEtrn(
            {
              cardId,
              titleType: TitleType.T3,
              fileName: title.fileName,
              file: title.content,
              signature,
              creationTime: title.creationTime,
            },
            {
              onSuccess: () => {
                setIsSigning(false);
                onSigned();
                history.push(MULTI_LOGISTICS_ETRN_TAB);
              },
              onError: () => {
                setIsSigning(false);
                addMessage(Messages.SigningError);
              },
            }
          );
        })
        .catch(e => {
          console.log(e);
          setIsSigning(false);
          addMessage(Messages.SigningError);
        });
    }
  };

  return (
    <Modal
      visible={visible}
      closable={!isLoading}
      onCancel={onClose}
      okText={i18.okText}
      okButtonProps={{ disabled: !thumbprint || isSigning }}
      onOk={handleOk}
      confirmLoading={isSigning}
      {...modalProps}
    >
      {messages === null ? (
        <Loading type="init" />
      ) : messages.length > 0 ? (
        <Error messages={messages} />
      ) : isSigning ? (
        <Loading type="signing" />
      ) : (
        <Certificates
          selected={thumbprint}
          list={certificates}
          onSelect={value => setThumbprint(value)}
        />
      )}
    </Modal>
  );
};

export default SignT3Modal;

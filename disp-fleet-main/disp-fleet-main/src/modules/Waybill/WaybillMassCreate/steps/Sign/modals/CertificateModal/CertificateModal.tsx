import React, { useState } from 'react';
import type { FC } from 'react';
import { Modal } from '@sber-sbertransport/ui-kit/src';
import { useTranslation } from 'i18n';

import { b64DecodeUnicode } from 'utils/utils';
import { signFileByCertificate } from 'utils/cryptoPro';
import { useCryptoPro } from 'hooks/useCryptoPro/useCryptoPro';
import { Messages } from 'constants/cryptoPro';

import { TMassCreateFirstTitleItem } from 'api/shifts/shifts.types';

import Loading from './Loading/Loading';
import Error from './Error/Error';
import Certificates from './Certificates/Certificates';

interface SignResult {
  signature: string;
  item: TMassCreateFirstTitleItem;
}

interface Props {
  visible: boolean;
  firstTitleResult: TMassCreateFirstTitleItem[] | undefined;
  onClose: () => void;
  onSign: (signatures: SignResult[]) => void;
  loading?: boolean;
}

const CertificateModal: FC<Props> = ({
  visible,
  firstTitleResult,
  onClose,
  onSign,
  loading,
}) => {
  const {
    messages, certificates, addMessage,
  } = useCryptoPro();
  const { sign: i18 } = useTranslation().t.Waybill.modal;
  const [thumbprint, setThumbprint] = useState<string | null>(null);

  const isLoading = !messages || loading;
  const modalProps = isLoading || messages?.length > 0
    ? { title: null, footer: null } : { title: i18.title };

  const signAndSend = () => {
    if (thumbprint && firstTitleResult && firstTitleResult.length > 0) {
      const signPromises = firstTitleResult.map(title => {
        const content = b64DecodeUnicode(title.content);
        return signFileByCertificate(thumbprint, content)
          .then((signature: string) => ({
            signature,
            item: title,
          }));
      });

      Promise.all(signPromises)
        .then((signatures: SignResult[]) => {
          onSign(signatures);
        })
        .catch(() => {
          addMessage(Messages.SigningError);
        });
    }
  };

  return (
    <Modal
      open={visible}
      closable={!isLoading}
      onCancel={onClose}
      okText={i18.okText}
      okButtonProps={{ disabled: !thumbprint }}
      onOk={signAndSend}
      destroyOnClose
      {...modalProps}
    >
      {messages === null ? (
        <Loading type="init" />
      ) : messages.length > 0 ? (
        <Error messages={messages} />
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

export default CertificateModal;

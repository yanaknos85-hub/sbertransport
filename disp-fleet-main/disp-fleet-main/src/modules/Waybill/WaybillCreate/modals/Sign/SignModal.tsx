import React, { useState } from 'react';
import type { FC } from 'react';
import { useHistory } from '@sber-sbertransport/mf-core';
import { Modal } from '@sber-sbertransport/ui-kit/src';
import * as routes from 'constants/routes.constants';
import { useTranslation } from 'i18n';
import { TCreateFirstTitleRequest, TCreateFirstTitleResponse } from 'api/waybill/waybill.types';
import type { UUID } from 'utils/io-ts';
import { b64DecodeUnicode } from 'utils/utils';
import { signFileByCertificate } from 'utils/cryptoPro';
import { useCryptoPro } from 'hooks/useCryptoPro/useCryptoPro';
import { Messages } from 'constants/cryptoPro';
import Loading from './Loading/Loading';
import Error from './Error/Error';
import Certificates from './Certificates/Certificates';
import { TitleType } from '../../../Waybill.constants';
import { useSendTitle } from 'api/waybill/waybill.api';

interface Props {
  visible: boolean;
  ewbUuid: UUID | null;
  titleForm: TCreateFirstTitleRequest | null;
  titleResult: TCreateFirstTitleResponse | null;
  onClose: () => void;
}

const SignModal: FC<Props> = ({
  visible,
  ewbUuid,
  titleForm,
  titleResult,
  onClose,
}) => {
  const history = useHistory();
  const {
    messages, certificates, addMessage,
  } = useCryptoPro();
  const [sendTitle] = useSendTitle();
  const { sign: i18 } = useTranslation().t.Waybill.modal;
  const [thumbprint, setThumbprint] = useState<string | null>(null);
  const [isSigning, setIsSigning] = useState(false);

  const isLoading = !messages || isSigning;
  const modalProps = isLoading || messages?.length > 0
    ? { title: null, footer: null } : { title: i18.title };

  const signAndSend = () => {
    if (thumbprint && ewbUuid && titleForm && titleResult) {
      setIsSigning(true);

      signFileByCertificate(thumbprint, b64DecodeUnicode(titleResult.content))
        .then((signature: string) => {
          sendTitle({
            ...titleResult,
            firstTitleForm: titleForm,
            ewbUuid,
            titleType: TitleType.FIRST,
            signature,
          })
            .then(() => history.push(routes.RELEASE_ON_LINE_LINK))
            .catch(() => setIsSigning(false));
        })
        .catch(() => {
          setIsSigning(false);
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

export default SignModal;

import React, { useState } from 'react';
import { Tooltip } from 'antd';
import { ReactComponent as CopyIdIcon } from '../../../../modules/Planner/images/copyIdIcon.svg';
import { useTranslation } from 'i18n';

import * as S from "./CopyId.styles";

interface Props {
  id: string;
  tooltipText: string; 
}

const CopyId = ({ id, tooltipText }: Props) => {
  const [copied, setCopied] = useState(false);
  const { t } = useTranslation();

  const handleCopy = () => {
    if (window.isSecureContext && navigator.clipboard) {
      navigator.clipboard
        .writeText(id)
        .then(() => {
          setCopied(true);
          setTimeout(() => setCopied(false), 1500);
        })
        .catch(error => {
          console.error('Ошибка копирования текста', error);
        });
    } else {
      const textArea = document.createElement('textarea');
      textArea.value = id;
      document.body.appendChild(textArea);
      textArea.focus();
      textArea.select();
      document.execCommand('copy');
      document.body.removeChild(textArea);
      setCopied(true);
      setTimeout(() => setCopied(false), 1500);
    }
  };

  return (
    <Tooltip title={copied ? t.Planner.numberIsCopied : tooltipText}>
      <S.CopyIdIconContainer>
      <CopyIdIcon onClick={handleCopy} />
      </S.CopyIdIconContainer>
    </Tooltip>
  );
};

export default CopyId;

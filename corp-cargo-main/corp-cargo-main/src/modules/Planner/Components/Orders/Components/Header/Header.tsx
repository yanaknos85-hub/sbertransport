import React, { FC, MouseEvent } from "react";
import moment from "moment";
import { DATE_FORMAT } from "constants/constants.app";
import CopyId from "modules/Planner/Components/OrderDetailed/CopyId";
import { useTranslation } from "i18n";

import * as S from "./Header.styles";

interface Props {
  desiredDate: string;
  humanReadableId: string;
  onClick: (e: MouseEvent<HTMLElement>) => void;
  value: string;
  contractorName: string | undefined;
}

export const Header: FC<Props> = (props) => {
  const { desiredDate, humanReadableId, contractorName } = props;
  const { t } = useTranslation();

  return (
    <S.Header>
      <S.HeaderText>
        <p>Отправка {moment(desiredDate).format(DATE_FORMAT.BASE_REVERTED_DOTS)}</p>
        <p>
          {humanReadableId}&nbsp;&nbsp;
            <CopyId id={humanReadableId} tooltipText={t.Planner.copyOrderId} />
        </p>
        <p>{contractorName}</p>
      </S.HeaderText>
    </S.Header>
  );
};


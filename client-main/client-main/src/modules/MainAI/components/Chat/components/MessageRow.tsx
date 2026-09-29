import React, { FC } from 'react';
import cn from 'classnames';
import { Link } from 'react-router-dom';
import type { TDraftDto } from 'api/draft-pilot/draft-pilot.types';
import { PASSENGER_TRIPS } from 'constants/constants.routes';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import Draft from '../../Draft/Draft';

import styles from '../Chat.module.scss';

interface MessageRowProps {
  id: string;
  text: string;
  isUser: boolean;
  draft: TDraftDto | null;
}

const MessageRow: FC<MessageRowProps> = ({
  id, text, isUser, draft,
}) => (
  <React.Fragment key={id}>
    <div
      className={cn(
        styles.message,
        {
          [styles.messageUser]: isUser,
          [styles.messageAssistant]: !isUser,
        }
      )}
    >
      {text}
    </div>

    {draft?.requestId && (
      <div className={cn(styles.message, styles.messageAssistant)}>
        Заявка <Link to={`${PASSENGER_TRIPS}/planned/${draft?.requestId}`}>№{draft?.humanReadableId}</Link>
      </div>
    )}

    {!isUser && draft && (
      <ErrorBoundary>
        <Draft draft={draft} />
      </ErrorBoundary>
    )}
  </React.Fragment>
);

export default MessageRow;

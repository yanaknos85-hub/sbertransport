import React, { FC } from 'react';
import { PassTrip } from 'api/trips/trips.types';
import Panel from 'components/Panel/Panel';
import withErrorBoundary from 'components/withErrorBoundary';
import { EMPTY_CELL_CONTENT } from 'constants/app.constants';

/** Комментарий */
export const CommentCard: FC<{
  requests: PassTrip['requests'];
  comment?: string;
}> = withErrorBoundary(({ requests, comment }) => {
  return (
    <Panel
      title="Комментарий к заказу"
      marginTop
      smallVerticalPadding
    >
      {requests.length ? (
        requests.map((request, index) => (
          <div key={request.id}>
            {requests.length > 1 && (
            <span>
              {index + 1}
              .
            </span>
            )}
            {' '}
            {request.commentForDriver ?? request.comment ?? EMPTY_CELL_CONTENT}
          </div>
        ))
      ) : (
        comment ?? EMPTY_CELL_CONTENT
      )}
    </Panel>
  );
});

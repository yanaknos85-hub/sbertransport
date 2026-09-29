import React from 'react';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';

const Empty = withErrorBoundary(() => <div>Раздел договоров не заполнен</div>);

export default Empty;

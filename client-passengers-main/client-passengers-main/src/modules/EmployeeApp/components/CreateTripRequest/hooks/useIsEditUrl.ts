import { useRouteMatch } from 'react-router-dom';

import { EDIT } from 'constants/constants.env';

export const useIsEditUrl = (): { isEditUrl: boolean } => {
  const match = useRouteMatch();
  const isEditUrl = match.path.split('/').includes(EDIT);

  return { isEditUrl };
};

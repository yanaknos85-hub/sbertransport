import * as React from 'react';
import { NavLink, useRouteMatch } from 'react-router-dom';
import { faChevronRight } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import * as R from 'ramda';
import styled from 'styled-components';

import { UUID } from 'utils/io-ts';
import { useTitle } from 'utils/useTitle';
import uuid from 'utils/uuid';

interface Breadcrumb {
  id: UUID;
  title: string;
  path: string;
}

interface BreadcrumbsContext {
  breadcrumbs: Breadcrumb[];
  register: (bc: Omit<Breadcrumb, 'id'>) => () => void;
}

const BreadcrumbsContext = React.createContext<BreadcrumbsContext | null>(null);

export const BreadcrumbsProvider: React.FC = ({ children }) => {
  const [state, setState] = React.useState<Breadcrumb[]>([]);

  const register = React.useCallback((bc: Omit<Breadcrumb, 'id'>): (() => void) => {
    const id = uuid();

    setState(R.append({ ...bc, id }));

    return () => setState(R.reject(R.whereEq({ id })));
  }, []);

  return <BreadcrumbsContext.Provider value={{ breadcrumbs: state, register }}>{children}</BreadcrumbsContext.Provider>;
};

export const useBreadcrumbs = (): BreadcrumbsContext => {
  const ctx = React.useContext(BreadcrumbsContext);

  if (ctx === null) {
    throw new Error('Breadcrumbs context requested but not provided');
  }

  return ctx;
};

export const Breadcrumb: React.FC<{ title: string }> = ({ title }) => {
  const { register } = useBreadcrumbs();
  const { path } = useRouteMatch();

  React.useEffect(() => register({ title, path }), [title, path, register]);

  return null;
};

const StyledBreadcrumbs = styled.nav`
  display: flex;
  height: 60px;
  align-items: center;
  flex: 0 0 auto;

  a {
    color: var(--matterhorn) !important;

    &:hover {
      background-color: inherit !important;
    }
  }
`;

const StyledBreadcrumb = styled(NavLink)`
  font-size: 16px;
  padding-left: 8px;
  padding-right: 8px;

  &:first-child {
    padding-left: 0;
  }
`;

// @ts-ignore
const Separator = () => <FontAwesomeIcon color="var(--matterhorn)" icon={faChevronRight} />;

export const Breadcrumbs: React.FC = () => {
  const { breadcrumbs } = useBreadcrumbs();

  const Prefix = '';

  useTitle(Prefix + (breadcrumbs.length > 0 ? ` | ${breadcrumbs.map(R.prop('title')).join(' - ')}` : ''), [
    breadcrumbs,
  ]);

  return (
    <StyledBreadcrumbs>
      {breadcrumbs.map(({
        id, title, path,
      }, idx) => (
        <React.Fragment key={id}>
          <StyledBreadcrumb to={path}>{title}</StyledBreadcrumb>
          {idx === breadcrumbs.length - 1 ? null : <Separator />}
        </React.Fragment>
      ))}
    </StyledBreadcrumbs>
  );
};

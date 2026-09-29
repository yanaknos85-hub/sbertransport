import * as React from 'react';
import { NavLink, useRouteMatch } from 'react-router-dom';
import { IconProp } from '@fortawesome/fontawesome-svg-core';
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

export interface BreadcrumbsContext {
  breadcrumbs: Breadcrumb[];
  register: (bc: Omit<Breadcrumb, 'id'>) => () => void;
  setState: React.Dispatch<React.SetStateAction<Breadcrumb[]>>;
}

export const BreadcrumbsContext = React.createContext<BreadcrumbsContext | null>(null);

export const useHook = () => {
  const [state, setState] = React.useState<Breadcrumb[]>([]);

  const register = React.useCallback((bc: Omit<Breadcrumb, 'id'>): (() => void) => {
    const id = uuid();

    setState(R.append({ ...bc, id }));

    return () => setState(R.reject(R.whereEq({ id })));
  }, []);

  return {
    breadcrumbs: state, register, setState,
  };
};

export const BreadcrumbsProvider: React.FC = ({ children }) => {
  const value = useHook();
  return <BreadcrumbsContext.Provider value={value}>{children}</BreadcrumbsContext.Provider>;
};

export const useBreadcrumbs = (): BreadcrumbsContext => {
  const ctx = React.useContext(BreadcrumbsContext);

  if (ctx === null) {
    throw new Error('Breadcrumbs context requested but not provided');
  }

  return ctx;
};

export const Breadcrumb: React.FC<{ title: string; path?: string }> = ({ title, path }) => {
  const { register } = useBreadcrumbs();
  const { path: routePath } = useRouteMatch();

  React.useEffect(() => register({ title, path: path || routePath }), [title, routePath, path, register]);

  return null;
};

const StyledBreadcrumbs = styled.nav`
  display: flex;
  height: 60px;
  align-items: center;
  flex: 0 0 auto;

    @media(max-width: 767px) {
      margin-left: 10px;
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

const Separator = () => <FontAwesomeIcon icon={faChevronRight as IconProp} />;

export const Breadcrumbs: React.FC = () => {
  const { breadcrumbs } = useBreadcrumbs();

  const Prefix = 'АС Сбертранспорт';

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

export const MaybeBreadcrumbs = () => {
  const breadcrumbs = useBreadcrumbs();
  return breadcrumbs.breadcrumbs.length >= 1 ? <Breadcrumbs /> : null;
};

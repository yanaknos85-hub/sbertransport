import React from 'react';
import { DownOutlined } from '@ant-design/icons';
import { Dropdown, Menu } from 'antd';

import {
  SortProperty, Sort, SortType, START_PAGE
} from './SortConstants';

import * as Styled from './SortMenu.styles';

interface Props {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onSortChange: any;
  setPagePagination: (page: number) => void;
}

export const SortMenu: React.FC<Props> = props => {
  const { onSortChange, setPagePagination } = props;

  const [label, setLabel] = React.useState<string>(`По дате отправления: ${Sort.DESC}`);

  const {
    Divider, Item, ItemGroup,
  } = Menu;

  const sortByParameters = (directionAsc: boolean, property: string): void => {
    onSortChange(directionAsc, property);

    switch (property) {
      case SortProperty.DESIRED_DATE:
        setLabel(`${SortType.BY_DESIRED_DATE}: ${directionAsc ? Sort.ASC : Sort.DESC}`);
        break;
      case SortProperty.REQUEST_HUMAN_ID:
        setLabel(`${SortType.REQUEST_HUMAN_ID}: ${directionAsc ? Sort.ASC : Sort.DESC}`);
        break;
      default:
        break;
    }
    setPagePagination(START_PAGE);
  };

  const menu = (
    <Menu>
      <ItemGroup title="По дате отправления">
        <Item>
          <Styled.Button
            block
            type="text"
            onClick={(): void => sortByParameters(false, SortProperty.DESIRED_DATE)}
          >
            {Sort.DESC}
          </Styled.Button>
        </Item>
        <Item>
          <Styled.Button
            block
            type="text"
            onClick={(): void => sortByParameters(true, SortProperty.DESIRED_DATE)}
          >
            {Sort.ASC}
          </Styled.Button>
        </Item>
      </ItemGroup>
      <Divider />
      <ItemGroup title="По номеру">
        <Item>
          <Styled.Button
            block
            type="text"
            onClick={(): void => sortByParameters(false, SortProperty.REQUEST_HUMAN_ID)}
          >
            {Sort.DESC}
          </Styled.Button>
        </Item>
        <Item>
          <Styled.Button
            block
            type="text"
            onClick={(): void => sortByParameters(true, SortProperty.REQUEST_HUMAN_ID)}
          >
            {Sort.ASC}
          </Styled.Button>
        </Item>
      </ItemGroup>
    </Menu>
  );

  return (
    <Styled.DropdownDiv>
      <Dropdown overlay={menu}>
        <Styled.DropdownButton type="text" onClick={(e: React.MouseEvent<HTMLElement>) => e.preventDefault()}>
          {label}
          {' '}
          <DownOutlined />
        </Styled.DropdownButton>
      </Dropdown>
    </Styled.DropdownDiv>
  );
};

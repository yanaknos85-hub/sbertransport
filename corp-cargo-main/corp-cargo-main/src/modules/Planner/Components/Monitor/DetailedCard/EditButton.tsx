import { ReactComponent as Cross } from '../../../images/crossIcon.svg';
import { EditOutlined } from '@ant-design/icons';
import React, { FC } from 'react';

interface Props {
  activeEdit: boolean;
  editMode: boolean;
  setIsEditMode: (value: boolean) => void;
}

export const EditButton: FC<Props> = (props) => {

  const { activeEdit, editMode, setIsEditMode } = props;

  const className = 'routeDetailed';

  if (!activeEdit) {
    return null;
  }

  return (
    <div
      className={`${className}__edit-button`}
      onClick={() => {
        setIsEditMode(!editMode)
      }}
    >
      {editMode ? <Cross/> : <EditOutlined/>}
    </div>
  )
};

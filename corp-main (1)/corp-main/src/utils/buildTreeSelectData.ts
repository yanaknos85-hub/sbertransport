import { IDepartment } from 'stores/Organizations/Organizations.interface';
import { UUID } from './io-ts';

export interface ITreeNode {
  key: UUID;
  id: UUID;
  value: string;
  title: string;
  children?: ITreeNode[];
}

const buildDepartmentTree = (departmentList: IDepartment[]): ITreeNode[] => {
  const addNodes = (parentId: UUID | null) => {
    const treeNode: ITreeNode[] = [];

    departmentList.forEach(item => {
      if (
        (parentId === null && !departmentList.some(d => d.id === item.parentId))
        || item.parentId === parentId
      ) {
        treeNode.push({
          key: item.id,
          id: item.id,
          value: item.id,
          title: item.departmentName,
          children: addNodes(item.id),
        });
      }
    });

    return treeNode;
  };

  return addNodes(null);
};

export { buildDepartmentTree };

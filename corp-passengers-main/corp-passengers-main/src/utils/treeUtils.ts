interface TreeNodeType {
  title?: string;
  key: number;
  children: TreeNodeType[];
}

export function createTree(arr: string[], key = 0): TreeNodeType[] {
  const result: TreeNodeType[] = [];

  if (arr.length < 1) {
    return result;
  }

  result[0] = {
    title: arr.shift(),
    children: createTree(arr, key + 1),
    key,
  };

  return result;
}

export const checkingImageFormat = (type: string): boolean => {
  const imageType = type.split('/')[1];
  const arrayType = ['png', 'jpg', 'jpeg', 'pdf', 'tiff', 'heif'];
  return arrayType.includes(imageType);
};

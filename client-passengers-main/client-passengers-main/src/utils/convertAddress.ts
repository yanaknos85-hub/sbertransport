export const convertAddress = (inputAddress: string) => {
  const addressParts = inputAddress.split(',').map(part => part.trim());
  const filteredAddressParts = addressParts.filter(part => part !== '');
  const newAddress = filteredAddressParts.slice(3).concat(filteredAddressParts.slice(2, 3)).join(', ');

  return newAddress;
};

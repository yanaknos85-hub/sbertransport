import { UUID } from 'utils/io-ts';

export function removeIdFromUrl(url: UUID) {
  const regex = /\/[0-9a-fA-F-]{36}$/;

  if (regex.test(url)) {
    return url.replace(regex, '');
  }

  return url;
}

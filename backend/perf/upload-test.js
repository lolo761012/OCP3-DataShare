import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 5,
  duration: '10s',
};

export default function () {
  const data = {
    file: http.file('DataShare performance test', 'perf-test.txt', 'text/plain'),
    expirationDays: '1',
  };

  const response = http.post('http://localhost:8080/api/files', data);

  check(response, {
    'status is 201': (r) => r.status === 201,
  });
}
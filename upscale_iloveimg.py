#!/usr/bin/env python3
import re, json, sys, os
from urllib.request import Request, urlopen
from urllib.parse import urlencode

UA = 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36'

def get_config():
    req = Request("https://www.iloveimg.com/upscale-image", headers={'User-Agent': UA})
    html = urlopen(req).read().decode()
    m = re.search(r'ilovepdfConfig\s*=\s*({.*?});', html, re.DOTALL)
    config = json.loads(m.group(1))
    m2 = re.search(r'ilovepdfConfig\.taskId\s*=\s*[\'"]([^\'"]+)[\'"]', html)
    config['taskId'] = m2.group(1)
    return config

def multipart_post(url, fields, file_data, filename, token):
    boundary = b'----BOUNDARY' + os.urandom(16).hex().encode()
    body = b''
    for k, v in fields.items():
        body += b'--' + boundary + b'\r\n'
        body += f'Content-Disposition: form-data; name="{k}"\r\n\r\n'.encode()
        body += str(v).encode() + b'\r\n'
    body += b'--' + boundary + b'\r\n'
    body += f'Content-Disposition: form-data; name="file"; filename="{filename}"\r\n'.encode()
    body += b'Content-Type: application/octet-stream\r\n\r\n'
    body += file_data + b'\r\n'
    body += b'--' + boundary + b'--\r\n'
    req = Request(url, data=body, headers={'User-Agent': UA})
    req.add_header('Authorization', f'Bearer {token}')
    req.add_header('Content-Type', f'multipart/form-data; boundary={boundary.decode()}')
    return json.loads(urlopen(req).read())

def form_post(url, fields, token, binary=False):
    body = urlencode(fields).encode()
    req = Request(url, data=body, headers={'User-Agent': UA})
    req.add_header('Authorization', f'Bearer {token}')
    req.add_header('Content-Type', 'application/x-www-form-urlencoded')
    resp = urlopen(req)
    return resp.read() if binary else json.loads(resp.read())

def main():
    if len(sys.argv) < 3:
        print(f"Usage: {sys.argv[0]} <image> <2|4> [output]")
        sys.exit(1)

    img_path = sys.argv[1]
    scale = int(sys.argv[2])
    output = sys.argv[3] if len(sys.argv) > 3 else f"upscaled_{scale}x_{os.path.basename(img_path)}"

    print("[1] Getting config...")
    cfg = get_config()
    token = cfg['token']
    task_id = cfg['taskId']
    server = f"https://{cfg['servers'][0]}.iloveimg.com/v1"
    print(f"    Server: {server}, Task: {task_id}")

    print("[2] Uploading image...")
    with open(img_path, 'rb') as f:
        file_data = f.read()
    filename = os.path.basename(img_path)
    up = multipart_post(f"{server}/upload", {"task": task_id}, file_data, filename, token)
    server_filename = up.get('server_filename')
    print(f"    Server filename: {server_filename}")

    print(f"[3] Upscaling {scale}x...")
    result = form_post(f"{server}/upscale", {
        "task": task_id,
        "server_filename": server_filename,
        "scale": str(scale),
    }, token, binary=True)

    with open(output, 'wb') as f:
        f.write(result)
    print(f"[4] Saved: {output}")

if __name__ == '__main__':
    main()

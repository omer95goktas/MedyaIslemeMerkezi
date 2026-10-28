using System;
using System.IO;
using System.Net;
using System.Net.Http;
using System.Threading.Tasks;

namespace EngelsizMedya
{
    public class ProgressStreamContent : HttpContent
    {
        private readonly Stream _content;
        private readonly int _bufferSize;
        private readonly Action<long, long> _progress;

        public ProgressStreamContent(Stream content, int bufferSize, Action<long, long> progress)
        {
            _content = content;
            _bufferSize = bufferSize;
            _progress = progress;
        }

        protected override async Task SerializeToStreamAsync(Stream stream, TransportContext context)
        {
            var buffer = new byte[_bufferSize];
            var size = _content.Length;
            var uploaded = 0L;

            while (true)
            {
                var length = await _content.ReadAsync(buffer, 0, buffer.Length);
                if (length <= 0) break;

                await stream.WriteAsync(buffer, 0, length);
                uploaded += length;
                if (_progress != null) _progress(uploaded, size);
            }
        }

        protected override bool TryComputeLength(out long length)
        {
            length = _content.Length;
            return true;
        }
    }
}

package egovframework.cmm.util;

public class CountingOutputStream extends java.io.OutputStream {

  private final java.io.OutputStream delegate;
  private long bytesWritten = 0L;

  public CountingOutputStream(java.io.OutputStream delegate) {
    this.delegate = delegate;
  }

  public long getBytesWritten() {
    return bytesWritten;
  }

  @Override
  public void write(int b) throws java.io.IOException {
    delegate.write(b);
    bytesWritten++;
  }

  @Override
  public void write(byte[] b) throws java.io.IOException {
    delegate.write(b);
    if (b != null)
      bytesWritten += b.length;
  }

  @Override
  public void write(byte[] b, int off, int len) throws java.io.IOException {
    delegate.write(b, off, len);
    if (len > 0)
      bytesWritten += len;
  }

  @Override
  public void flush() throws java.io.IOException {
    delegate.flush();
  }

  @Override
  public void close() throws java.io.IOException {
    delegate.close();
  }
}

